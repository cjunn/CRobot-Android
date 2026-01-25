//
// Created by 86152 on 2025/12/11.
//
#include <stddef.h>
#include "cjs.h"
#include "../quickjs/cutils.h"
#include "../core/bootstrap.h"
#include "../core/jvm.h"
#include "../core/zip.h"
#include <thread>
#include <string>
#include <regex>
#include <iostream>
#include <string>
#include <sstream> // 字符串流头文件

typedef struct {
    jobject object;
    jmethodID gcMet;
    jmethodID invokeMet;
    size_t len;
    jobject *methods;
    char **signs;
} Apt;

typedef struct {
    std::string code;
    std::string message;
    std::string traceback;
} ErrInfo;

static Cjs *GetCjs(JSRuntime *rt) {
    return ((Cjs *) JS_GetRuntimeOpaque(rt));
}

static JSValue GetCLib(JSContext *ctx) {
    return JS_GetGlobalObject(ctx);
}

static void LoadZip(zip_t *zip, std::string &str) {
    const char *codeBody = NULL;
    size_t codeLen = 0;
    zip_entry_read(zip, (void **) &codeBody, &codeLen);
    zip_entry_close(zip);
    zip_stream_close(zip);
    if (codeBody) {
        str.append(codeBody, codeLen);
        delete codeBody;
    }
}

static JSValue JS_CreateError(JSContext *ctx, const char *chars) {
    JSValue error = JS_NewError(ctx);
    JSValue message = JS_DupValue(ctx, JS_NewString(ctx, chars));
    JS_SetPropertyStr(ctx, error, "message", message);
    JS_FreeValue(ctx, message);
    return error;
}
static JSValue JS_ThrowRuntimeError(JSContext *ctx,const char *fmt, ...){
    JSValue val;
    va_list ap;
    va_start(ap, fmt);
    char buf[256];
    vsnprintf(buf, sizeof(buf), fmt, ap);
    val = JS_Throw(ctx, JS_CreateError(ctx,buf));
    va_end(ap);
    return val;
}


static JSModuleDef *JsModuleLoader(JSContext *ctx, const char *module_name, void *opaque) {
    JSRuntime *rt = JS_GetRuntime(ctx);
    Cjs *cjs = GetCjs(rt);
    if (cjs->zipLen == 0) {
        JS_ThrowReferenceError(ctx, "Un Init!");
        return NULL;
    }
    std::string module = std::string(module_name);
    std::replace(module.begin(), module.end(), '.', '/');
    module.append(".js");
    struct zip_t *zip = zip_stream_open(cjs->zip, cjs->zipLen, 0, 'r');
    if (!zip) {
        JS_ThrowReferenceError(ctx, "The dependency table does not exist!");
        return NULL;
    }
    if (zip_entry_open(zip, module.c_str()) != 0) {
        std::string info;
        info.append("The module \"");
        info.append(module);
        info.append("\" does not exist");
        JS_ThrowReferenceError(ctx, "%s", info.c_str());
        return NULL;
    }
    std::string code;
    LoadZip(zip, code);
    JSValue val = JS_Eval(ctx, code.c_str(), code.size(), module_name,
                          JS_EVAL_TYPE_MODULE | JS_EVAL_FLAG_COMPILE_ONLY);
    if (JS_IsException(val)) {
        JS_FreeValue(ctx, val);
        return NULL;
    }
    JSModuleDef *ret = (JSModuleDef *) JS_VALUE_GET_PTR(val);
    JS_FreeValue(ctx, val);
    return ret;
}

static void JsStdPromiseRejectionTracker(JSContext *ctx, JSValueConst promise, JSValueConst reason, BOOL is_handled, void *opaque) {

}

static int JsInterruptHandler(JSRuntime *rt, void *opaque) {
    if (GetCjs(rt)->closed) {
        return 1;
    }
    return 0;
}

static void DumpArgAppend(void *opaque, const char *buf, size_t len) {
    std::string *buff = static_cast<std::string *>(opaque);
    buff->append(buf, len);
}

static void JSDumpArgs(JSContext *ctx, std::string &line, JSValueConst *argv, int argc) {
    int i;
    JSValueConst v;
    for (i = 0; i < argc; i++) {
        if (i != 0)
            line.append(" ");
        v = argv[i];
        if (JS_IsString(v)) {
            const char *str;
            size_t len;
            str = JS_ToCStringLen(ctx, &len, v);
            if (!str)
                return;
            line.append(str);
            JS_FreeCString(ctx, str);
        } else {
            std::string buff;
            JS_PrintValue(ctx, DumpArgAppend, &buff, v, NULL);
            line.append(buff);
        }
    }
}

static void JSDumpArg(JSContext *ctx, std::string &line, JSValueConst arg) {
    JSValueConst args[] = {arg};
    JSDumpArgs(ctx, line, args, 1);
}

static JSValue CheckArgType(JSContext *ctx, int index, int argc, JSValueConst *argv, JS_BOOL(*func)(JSValueConst), const char *name) {
    std::string ret;
    if (index > argc - 1) {
        return JS_ThrowRuntimeError(ctx,"The %d parameter is empty.",index);
    }
    if (!func(argv[index])) {
        return JS_ThrowRuntimeError(ctx,"The %d parameter expect %s.",index,name);
    }
    return JS_NULL;
}

#define CheckArgType2(ctx, index, argc, argv, func, name) \
ret = CheckArgType(ctx,index,argc,argv,func,name); \
if(JS_HasException(ctx)){ \
return ret; \
}

static JSValue NewFuncApt(JNIEnv *env, JSContext *ctx, jobject value);
static JSValue NewObjApt(JNIEnv *env, JSContext *ctx, jobject value);
static class J2JS {
    JSContext *ctx;
    JNIEnv *env;
public:
    J2JS(JSContext *ctx, JNIEnv *env) : ctx(ctx), env(env) {

    }

    JSValue newInt(jobject data) {
        return JS_NewInt32(ctx, ToInt(env, data));
    }

    JSValue newFloat(jobject data) {
        return JS_NewFloat64(ctx, ToFloat(env, data));
    }

    JSValue newBool(jobject data) {
        return JS_NewBool(ctx, ToBool(env, data));
    }

    JSValue newLong(jobject data) {
        return JS_NewInt64(ctx, ToLong(env, data));
    }

    JSValue newDouble(jobject data) {
        return JS_NewFloat64(ctx, ToDouble(env, data));
    }

    JSValue newVoid() {
        return JS_NULL;
    }

    JSValue newString(jobject data) {
        jstring string = static_cast<jstring>(data);
        const char *chars = env->GetStringUTFChars(string, NULL);
        JSValue val = JS_NewString(ctx, chars);
        env->ReleaseStringUTFChars(string, chars);
        return val;
    }

    JSValue newArray(jobject data) {
        JSValue jsNewArray = JS_NewArray(ctx);
        JArray array(env,static_cast<jobjectArray>(data));
        for (int i = 0; i < array.size(); i++) {
            JLocal<jobject> element = JLocal<jobject>(env,array.get(i));
            J2JS j2Js(ctx,env);
            JSValue any = j2Js.newAny(element.get());
            JS_DefinePropertyValueUint32(ctx, jsNewArray, i, any, JS_PROP_C_W_E);
        }
        return jsNewArray;
    }


    JSValue newByte(jobject data) {
        return JS_NewInt32(ctx, ToByte(env, data));
    }

    JSValue newShort(jobject data) {
        return JS_NewInt32(ctx, ToShort(env, data));
    }

    JSValue newBytes(jobject data) {
        JBytes bytes(env,static_cast<jbyteArray>(data));
        return JS_NewArrayBufferCopy(ctx, (unsigned char *)(bytes.str()), bytes.size());
    }


    JSValue newJsonBean(jobject data) {
        std::string json;
        JsonBeanToString(env,data,json);
        return JS_ParseJSON(ctx, json.c_str(), json.size(), "<input>");
    }


    JSValue newFuncApt(jobject data) {
        return NewFuncApt(env, ctx, data);
    }

    JSValue newObjApt(jobject data) {
        return NewObjApt(env, ctx, data);
    }

    JSValue newObjectPtr(jobject data) {
        Cjs* cjs = GetCjs(JS_GetRuntime(ctx));
        void* ptr = ToObjectPtr(env,data);
        JSValue ret = JS_MKPTR(JS_TAG_OBJECT,ptr);
        return ret;
    }

    JSValue newAny(jobject data) {
        if (IsNull(data)) {
            return this->newVoid();
        } else if (IsArray(env, data)) {
            return this->newArray(data);
        } else if (IsString(env, data)) {
            return this->newString(data);
        } else if (IsBool(env, data)) {
            return this->newBool(data);
        } else if (IsInt(env, data)) {
            return this->newInt(data);
        } else if (IsLong(env, data)) {
            return this->newLong(data);
        } else if (IsFloat(env, data)) {
            return this->newFloat(data);
        } else if (IsDouble(env, data)) {
            return this->newDouble(data);
        } else if (IsByte(env, data)) {
            return this->newByte(data);
        } else if (IsShort(env, data)) {
            return this->newShort(data);
        } else if (IsFuncApt(env, data)) {
            return this->newFuncApt(data);
        } else if (IsObjApt(env, data)) {
            return this->newObjApt(data);
        } else if (IsObjectPtr(env, data)) {
            return this->newObjectPtr(data);
        } else if (IsBytes(env, data)) {
            return this->newBytes(data);
        } else if (ISJsonBean(env, data)) {
            return this->newJsonBean(data);
        } else {
            return this->newVoid();
        }
    }
};

static void UpdateImageIfNeed(JSContext *ctx){
    JSValue C = GetCLib(ctx);
    JSValue Display = JS_GetPropertyStr(ctx,C,"Display");
    JSValue update = JS_GetPropertyStr(ctx,Display,"updateIfNeed");
    JS_Call(ctx,update,Display,0, nullptr);
    JS_FreeValue(ctx,update);
    JS_FreeValue(ctx,Display);
    JS_FreeValue(ctx,C);
}

static void CreateImageLib(JSContext *ctx) {
    JSValue C = GetCLib(ctx);
    JSValue Screen = JS_DupValue(ctx, JS_NewObject(ctx));
    JS_SetPropertyStr(ctx, C, "Screen", Screen);
    JS_FreeValue(ctx, C);

    JSValue func = JS_UNDEFINED;

    #define METHOD(f,cnt) \
    func = JS_DupValue(ctx, JS_NewCFunction(ctx, f, #f, cnt));\
    JS_SetPropertyStr(ctx, Screen, #f, func);\
    JS_FreeValue(ctx, func);\


    auto getPixelColor = [](JSContext *ctx, JSValueConst this_val, int argc, JSValueConst *argv) -> JSValue {
        JSValue ret = JS_NULL;
        CheckArgType2(ctx, 0, argc, argv, JS_IsNumber, "int");
        CheckArgType2(ctx, 1, argc, argv, JS_IsNumber, "int");
        UpdateImageIfNeed(ctx);
        JSRuntime *rt = JS_GetRuntime(ctx);
        Cjs *cjs = GetCjs(rt);
        Bitmap *bitmap = cjs->bitmap;
        int x = 0;
        JS_ToInt32(ctx, &x, argv[0]);
        int y = 0;
        JS_ToInt32(ctx, &y, argv[1]);
        Color out;
        GetSimpleColor(bitmap, x, y, &out);
        return JS_NewInt32(ctx, out.hex);
    };
    METHOD(getPixelColor,2);

    auto findColor = [](JSContext *ctx, JSValueConst this_val, int argc, JSValueConst *argv) -> JSValue {
        JSValue ret = JS_NULL;
        CheckArgType2(ctx, 0, argc, argv, JS_IsNumber, "int");
        CheckArgType2(ctx, 1, argc, argv, JS_IsNumber, "int");
        CheckArgType2(ctx, 2, argc, argv, JS_IsNumber, "int");
        CheckArgType2(ctx, 3, argc, argv, JS_IsNumber, "int");
        CheckArgType2(ctx, 4, argc, argv, JS_IsString, "string");
        CheckArgType2(ctx, 5, argc, argv, JS_IsNumber, "int");
        CheckArgType2(ctx, 6, argc, argv, JS_IsNumber, "double");
        UpdateImageIfNeed(ctx);
        JSRuntime *rt = JS_GetRuntime(ctx);
        Cjs *cjs = GetCjs(rt);
        Bitmap *bitmap = cjs->bitmap;
        int x = 0;
        int y = 0;
        int x1 = 0;
        int y1 = 0;
        int dir = 0;
        double sim = 0;
        JS_ToInt32(ctx, &x, argv[0]);
        JS_ToInt32(ctx, &y, argv[1]);
        JS_ToInt32(ctx, &x1, argv[2]);
        JS_ToInt32(ctx, &y1, argv[3]);
        const char *color = JS_ToCString(ctx, argv[4]);
        JS_ToInt32(ctx, &dir, argv[5]);
        JS_ToFloat64(ctx, &sim, argv[6]);
        Point out;
        FindColor(bitmap, x, y, x1, y1, color, sim, dir, &out);
        ret = JS_NewArray(ctx);
        JS_DefinePropertyValueUint32(ctx, ret, 0, JS_NewInt32(ctx, out.x), JS_PROP_C_W_E);
        JS_DefinePropertyValueUint32(ctx, ret, 1, JS_NewInt32(ctx, out.y), JS_PROP_C_W_E);
        JS_FreeCString(ctx, color);
        return ret;
    };
    METHOD(findColor,7);

    auto findMultiColor = [](JSContext *ctx, JSValueConst this_val, int argc, JSValueConst *argv) -> JSValue {
        JSValue ret = JS_NULL;
        CheckArgType2(ctx, 0, argc, argv, JS_IsNumber, "int");
        CheckArgType2(ctx, 1, argc, argv, JS_IsNumber, "int");
        CheckArgType2(ctx, 2, argc, argv, JS_IsNumber, "int");
        CheckArgType2(ctx, 3, argc, argv, JS_IsNumber, "int");
        CheckArgType2(ctx, 4, argc, argv, JS_IsString, "string");
        CheckArgType2(ctx, 5, argc, argv, JS_IsString, "string");
        CheckArgType2(ctx, 6, argc, argv, JS_IsNumber, "int");
        CheckArgType2(ctx, 7, argc, argv, JS_IsNumber, "double");
        UpdateImageIfNeed(ctx);
        JSRuntime *rt = JS_GetRuntime(ctx);
        Cjs *cjs = GetCjs(rt);
        Bitmap *bitmap = cjs->bitmap;

        int x = 0;
        int y = 0;
        int x1 = 0;
        int y1 = 0;
        int dir = 0;
        double sim = 0;
        JS_ToInt32(ctx, &x, argv[0]);
        JS_ToInt32(ctx, &y, argv[1]);
        JS_ToInt32(ctx, &x1, argv[2]);
        JS_ToInt32(ctx, &y1, argv[3]);
        const char *first_color = JS_ToCString(ctx, argv[4]);
        const char *feature = JS_ToCString(ctx, argv[5]);
        JS_ToInt32(ctx, &dir, argv[6]);
        JS_ToFloat64(ctx, &sim, argv[7]);

        Point point;
        FindFeature(bitmap, x, y, x1, y1, first_color, feature, sim, dir, &point);

        JS_FreeCString(ctx, first_color);
        JS_FreeCString(ctx, feature);

        ret = JS_NewArray(ctx);
        JS_DefinePropertyValueUint32(ctx, ret, 0, JS_NewInt32(ctx, point.x), JS_PROP_C_W_E);
        JS_DefinePropertyValueUint32(ctx, ret, 1, JS_NewInt32(ctx, point.y), JS_PROP_C_W_E);
        return ret;
    };
    METHOD(findMultiColor,8);

    auto findComplexMultiColor = [](JSContext *ctx, JSValueConst this_val, int argc, JSValueConst *argv) -> JSValue {
        JSValue ret = JS_NULL;
        CheckArgType2(ctx, 0, argc, argv, JS_IsNumber, "int");
        CheckArgType2(ctx, 1, argc, argv, JS_IsNumber, "int");
        CheckArgType2(ctx, 2, argc, argv, JS_IsNumber, "int");
        CheckArgType2(ctx, 3, argc, argv, JS_IsNumber, "int");
        CheckArgType2(ctx, 4, argc, argv, JS_IsString, "string");
        CheckArgType2(ctx, 5, argc, argv, JS_IsString, "string");
        CheckArgType2(ctx, 6, argc, argv, JS_IsString, "string");
        CheckArgType2(ctx, 7, argc, argv, JS_IsNumber, "int");
        CheckArgType2(ctx, 8, argc, argv, JS_IsNumber, "double");
        CheckArgType2(ctx, 9, argc, argv, JS_IsNumber, "double");
        UpdateImageIfNeed(ctx);
        JSRuntime *rt = JS_GetRuntime(ctx);
        Cjs *cjs = GetCjs(rt);
        Bitmap *bitmap = cjs->bitmap;

        int x = 0;
        int y = 0;
        int x1 = 0;
        int y1 = 0;
        int dir = 0;
        double sim = 0;
        double tolerance = 0;
        JS_ToInt32(ctx, &x, argv[0]);
        JS_ToInt32(ctx, &y, argv[1]);
        JS_ToInt32(ctx, &x1, argv[2]);
        JS_ToInt32(ctx, &y1, argv[3]);
        const char *first_color = JS_ToCString(ctx, argv[4]);
        const char *feature = JS_ToCString(ctx, argv[5]);
        const char *not_feature = JS_ToCString(ctx, argv[6]);
        JS_ToInt32(ctx, &dir, argv[7]);
        JS_ToFloat64(ctx, &sim, argv[8]);
        JS_ToFloat64(ctx, &tolerance, argv[9]);

        Point point;
        FindComplexFeature(bitmap, x, y, x1, y1, first_color, feature, not_feature, sim, tolerance,
                           dir, &point);

        JS_FreeCString(ctx, first_color);
        JS_FreeCString(ctx, feature);
        JS_FreeCString(ctx, not_feature);

        ret = JS_NewArray(ctx);
        JS_DefinePropertyValueUint32(ctx, ret, 0, JS_NewInt32(ctx, point.x), JS_PROP_C_W_E);
        JS_DefinePropertyValueUint32(ctx, ret, 1, JS_NewInt32(ctx, point.y), JS_PROP_C_W_E);
        return ret;
    };
    METHOD(findComplexMultiColor,10);

    auto cmpColor = [](JSContext *ctx, JSValueConst this_val, int argc, JSValueConst *argv) -> JSValue {
        JSValue ret = JS_NULL;
        CheckArgType2(ctx, 0, argc, argv, JS_IsNumber, "int");
        CheckArgType2(ctx, 1, argc, argv, JS_IsNumber, "int");
        CheckArgType2(ctx, 2, argc, argv, JS_IsString, "string");
        CheckArgType2(ctx, 3, argc, argv, JS_IsNumber, "double");
        UpdateImageIfNeed(ctx);
        JSRuntime *rt = JS_GetRuntime(ctx);
        Cjs *cjs = GetCjs(rt);
        Bitmap *bitmap = cjs->bitmap;
        int x = 0;
        int y = 0;
        double sim = 0;
        JS_ToInt32(ctx, &x, argv[0]);
        JS_ToInt32(ctx, &y, argv[1]);
        const char *str = JS_ToCString(ctx, argv[2]);
        JS_ToFloat64(ctx, &sim, argv[3]);
        ret = JS_NewBool(ctx, CmpColor(bitmap, x, y, str, sim));
        JS_FreeCString(ctx, str);
        return ret;
    };
    METHOD(cmpColor,4);

    JS_FreeValue(ctx, Screen);
}

static JSValue RedirectCallerObj(JSContext *ctx, JSValueConst this_val, int argc, JSValueConst *argv, int magic);

static JSValue RedirectCallerMet(JSContext *ctx, JSValueConst this_val, int argc, JSValueConst *argv, int magic, JSValue *func_data);

static jmethodID GetJMethodID(JNIEnv *env, jclass hClz, jstring name, jstring sign) {
    JString methodName(env, name);
    JString methodSign(env, sign);
    return env->GetMethodID(hClz, methodName.str(), methodSign.str());
}

static JSValue
NewObjApt(JNIEnv *env, JSContext *ctx, JSClassID aptClass,
          jobject object,
          jstring gcName, jstring gcSign,
          jstring invokeName, jstring invokeSign,
          jobjectArray methods,
          jobjectArray fields) {
    JSValue aptJs = JS_NewObjectClass(ctx, aptClass);
    Apt *apt = (Apt *) malloc(sizeof(Apt));
    JArray callListRef(env, methods);
    jclass hClz = env->GetObjectClass(object);
    jsize callSize = callListRef.size();
    apt->object = env->NewGlobalRef(object);
    apt->gcMet = GetJMethodID(env, hClz, gcName, gcSign);
    apt->invokeMet = GetJMethodID(env, hClz, invokeName, invokeSign);
    apt->len = callSize;
    apt->methods = new jobject[callSize];
    apt->signs = new char *[callSize];
    JS_SetOpaque(aptJs, apt);
    //适配方法调用
    for (int i = 0; i < callSize; i++) {
        JLocal<jobjectArray> item(env, static_cast<jobjectArray>(callListRef.get(i)));
        JArray callRef(env, item.get());
        JLocal<jobject> method(env, callRef.get(0));
        JLocal<jstring> callName(env, static_cast<jstring>(callRef.get(1)));
        JLocal<jstring> callSign(env, static_cast<jstring>(callRef.get(2)));
        JString callNameS(env, callName.get());
        JString callSignS(env, callSign.get());
        apt->methods[i] = env->NewGlobalRef(method.get());
        apt->signs[i] = strdup(callSignS.str());
        JSValue func = JS_DupValue(ctx, JS_NewCFunctionMagic(ctx, RedirectCallerObj, callNameS.str(), 0, JS_CFUNC_generic_magic, i));
        JS_SetPropertyStr(ctx, aptJs, callNameS.str(), func);
        JS_FreeValue(ctx, func);
    }
    if(fields== nullptr){
        return aptJs;
    }
    //适配字段值
    JArray fieldsRef(env, fields);
    jsize fieldSize = fieldsRef.size();
    for(int i=0;i<fieldSize;i++){
        JLocal<jobjectArray> item(env, static_cast<jobjectArray>(fieldsRef.get(i)));
        JArray fieldRef(env, item.get());
        JLocal<jobject> name(env, fieldRef.get(0));
        JLocal<jobject> value(env, fieldRef.get(1));
        JString javName(env,static_cast<jstring>(name.get()));
        J2JS j2Js(ctx,env);
        JSValue jsValue = j2Js.newAny(value.get());
        JS_SetPropertyStr(ctx, aptJs, javName.str(), jsValue);
    }
    return aptJs;
}

static JSValue NewObjApt(JNIEnv *env, JSContext *ctx, jobject value) {
    JSRuntime *rt = JS_GetRuntime(ctx);
    Cjs *cjs = GetCjs(rt);
    JLocal<jobjectArray> _info(env, UnpackJniBaseApt(env, value));
    JArray info(env, _info.get());
    JLocal<jobject> apt(env, info.get(0));
    JLocal<jobjectArray> gc(env, static_cast<jobjectArray>(info.get(1)));
    JLocal<jobjectArray> invoke(env, static_cast<jobjectArray>(info.get(2)));
    JLocal<jobjectArray> methods(env, static_cast<jobjectArray>(info.get(3)));
    JLocal<jobjectArray> fields(env, static_cast<jobjectArray>(info.get(4)));
    JArray gcList(env, gc.get());
    JLocal<jstring> gcName(env, static_cast<jstring>(gcList.get(0)));
    JLocal<jstring> gcSign(env, static_cast<jstring>(gcList.get(1)));
    JArray invokeList(env, invoke.get());
    JLocal<jstring> invokeName(env, static_cast<jstring>(invokeList.get(0)));
    JLocal<jstring> invokeSign(env, static_cast<jstring>(invokeList.get(1)));
    return NewObjApt(env, ctx, cjs->aptClass,
                     apt.get(),
                     gcName.get(), gcSign.get(),
                     invokeName.get(), invokeSign.get(),
                     methods.get(),fields.get());
}

static JSValue NewFuncApt(JNIEnv *env, JSContext *ctx, jobject value) {
    JSValueConst arr[1] = {NewObjApt(env, ctx, value)};
    JSValue ret = JS_NewCFunctionData(ctx, RedirectCallerMet, 0, 0, 1, arr);
    JS_FreeValue(ctx, arr[0]);
    return ret;
}

static void ExtractLogInfo(const std::string &input, std::string &source, int &lineNum) {
    source = "";
    lineNum = -1;
    const size_t inputLen = input.size();
    size_t pos = 0;          // 当前遍历位置
    int validLineCount = 0;  // 有效行计数器（非空、非纯空白行）
    std::string targetLine;  // 存储第3行的有效内容
    while (pos < inputLen) {
        size_t newlinePos = input.find('\n', pos);
        if (newlinePos == std::string::npos) {
            newlinePos = inputLen; // 最后一行无换行符，结束位置为字符串末尾
        }
        std::string currentLineStr = input.substr(pos, newlinePos - pos);
        size_t start = currentLineStr.find_first_not_of(" \t");
        size_t end = currentLineStr.find_last_not_of(" \t");
        std::string trimmedLine = (start == std::string::npos) ? "" : currentLineStr.substr(start,
                                                                                            end -
                                                                                            start +
                                                                                            1);
        if (!trimmedLine.empty()) {
            validLineCount++;
            if (validLineCount == 3) {
                targetLine = trimmedLine;
                break;
            }
        }
        pos = newlinePos + 1;
    }
    if (validLineCount < 3) {
        return;
    }
    size_t openParen = targetLine.find('(');
    if (openParen == std::string::npos) return;
    size_t closeParen = targetLine.find(')', openParen);
    if (closeParen == std::string::npos) return;
    std::string parenContent = targetLine.substr(openParen + 1, closeParen - openParen - 1);

    size_t firstColon = parenContent.find(':');
    if (firstColon == std::string::npos) return;
    source = parenContent.substr(0, firstColon);

    size_t secondColon = parenContent.find(':', firstColon + 1);
    if (secondColon == std::string::npos) {
        source = "";
        return;
    }
    std::string lineNumStr = parenContent.substr(firstColon + 1, secondColon - firstColon - 1);
    try {
        lineNum = std::stoi(lineNumStr);
    } catch (const std::exception &e) {

    }
}

static void CurrentLogInfo(JSContext *ctx, std::string &source, int &lineNum) {
    Cjs* cjs= GetCjs(JS_GetRuntime(ctx));

    const char *Error = "new Error();";
    JSValue error = JS_Eval(ctx, Error, strlen(Error), "error", JS_EVAL_TYPE_GLOBAL);
    JSValue stack = JS_GetPropertyStr(ctx, error, "stack");
    if (JS_IsString(stack)) {
        const char *stackStr = JS_ToCString(ctx, stack);
        ExtractLogInfo(stackStr, source, lineNum);
        if (cjs->lineNumber > 0 && cjs->fileName == source) {
            lineNum = lineNum + cjs->lineNumber - 1;
        }
        source.append(".js");
        JS_FreeCString(ctx, stackStr);
    }
    JS_FreeValue(ctx, stack);
    JS_FreeValue(ctx, error);
}

static JSValue CallLogPrint(JSContext *ctx, int argc, JSValueConst *argv, const char *level) {
    JSValue ret = JS_UNDEFINED;
    std::string message;
    JSDumpArgs(ctx, message, argv, argc);
    JSValue C = GetCLib(ctx);
    JSValue output = JS_GetPropertyStr(ctx, C, "output");
    if (JS_IsFunction(ctx, output)) {
        std::string source;
        int currentLine = 0;
        CurrentLogInfo(ctx, source, currentLine);
        JSValueConst argSource = JS_NewString(ctx, source.c_str());
        JSValueConst argCurrentLine = JS_NewInt64(ctx, currentLine);
        JSValueConst argMessage = JS_NewString(ctx, message.c_str());
        JSValueConst argTag = JS_NewString(ctx, level);
        JSValueConst nArgv[4];
        nArgv[0] = argSource;
        nArgv[1] = argCurrentLine;
        nArgv[2] = argMessage;
        nArgv[3] = argTag;
        JSValue res = JS_Call(ctx, output, C, 4, nArgv);
        if (JS_IsException(res)) {
            ret = res;
        }
        JS_FreeValue(ctx, argSource);
        JS_FreeValue(ctx, argCurrentLine);
        JS_FreeValue(ctx, argMessage);
        JS_FreeValue(ctx, argTag);
    }
    JS_FreeValue(ctx, output);
    JS_FreeValue(ctx, C);
    return ret;
}

static JSValue CallLogInfo(JSContext *ctx, JSValueConst this_val, int argc, JSValueConst *argv) {
    return CallLogPrint(ctx, argc, argv, "INFO");
}

static void CreateConsoleLib(JSContext *ctx) {
    JSValue console = JS_DupValue(ctx, JS_NewObject(ctx));
    JSValue global = JS_GetGlobalObject(ctx);

    JSValue func = JS_DupValue(ctx, JS_NewCFunction(ctx, CallLogInfo, "log", 0));
    JS_SetPropertyStr(ctx, console, "log", func);
    JS_FreeValue(ctx, func);

    JS_SetPropertyStr(ctx, global, "console", console);
    JS_FreeValue(ctx, console);
    JS_FreeValue(ctx, global);
}

static bool CheckJvmError(JNIEnv *env,JSContext *ctx){
    jstring message = DumpJvmException(env);
    if(message!= nullptr){
        JString msg(env,message);
        JS_ThrowRuntimeError(ctx,msg.str());
        return true;
    }
    return false;
}

static void CreateAptLib(JSContext *ctx){
    JSValue C = GetCLib(ctx);
    auto wrap = [](JSContext *ctx, JSValueConst this_val, int argc, JSValueConst *argv) -> JSValue {
        Cjs *cjs = GetCjs(JS_GetRuntime(ctx));
        if (argc == 0 || JS_GetClassID(argv[0]) != cjs->aptClass) {
            JS_ThrowRuntimeError(ctx, "The %d expectation is a %s value!", 1, "apt");
            return JS_EXCEPTION;
        }
        JSValue values[2];
        JSValue promise = JS_NewPromiseCapability(ctx, values);
        JSValue resolve = values[0];
        JSValue reject = values[1];
        JSValue C = GetCLib(ctx);
        JSValue Apt = JS_GetPropertyStr(ctx, C, "Apt");
        JSValue wrap = JS_GetPropertyStr(ctx, Apt, "wrap");
        JSValueConst p[3] = {argv[0], resolve, reject};
        JS_Call(ctx, wrap, Apt, 3, p);
        bool isErr = false;
        if (CheckJvmError(cjs->env, ctx)) {
            JS_FreeValue(ctx, reject);
            JS_FreeValue(ctx, promise);
            JS_FreeValue(ctx, resolve);
            isErr = true;
        } else {
            cjs->gc.insert(JS_VALUE_GET_PTR(resolve));
            cjs->gc.insert(JS_VALUE_GET_PTR(reject));
        }
        JS_FreeValue(ctx, wrap);
        JS_FreeValue(ctx, Apt);
        JS_FreeValue(ctx, C);
        return isErr ? JS_EXCEPTION : promise;
    };
    JSValue func = JS_DupValue(ctx, JS_NewCFunction(ctx, wrap, "wrap", 4));
    JS_SetPropertyStr(ctx, C, "wrap", func);
    JS_FreeValue(ctx, func);
    JS_FreeValue(ctx, C);
}

static JSContext *JsNewCustomContext(JSRuntime *rt) {
    JSContext *ctx = JS_NewContext(rt);
    CreateImageLib(ctx);
    CreateConsoleLib(ctx);
    CreateAptLib(ctx);
    return ctx;
}

static JSValue JS_ThrowInterrupted(JSContext *ctx) {
    JSValue error = JS_ThrowInternalError(ctx, "interrupted");
    JS_SetUncatchableException(ctx, TRUE);
    return error;
}

static void CancelAllGc(Cjs *cjs, JSContext *ctx) {
    JNIEnv *env = cjs->env;
    std::unordered_set<void *> pending = cjs->gc;
    for (auto it = pending.begin(); it != pending.end(); it++) {
        void *ptr = *it;
        JSValue ret = JS_MKPTR(JS_TAG_OBJECT,ptr);
        JS_FreeValue(ctx, ret);
    }
    pending.clear();
}

static void ParseAndExecuteApt(JSContext *ctx,JSValue res){
    Cjs *cjs = GetCjs(JS_GetRuntime(ctx));
    JSValue data = JS_GetPropertyUint32(ctx, res, 0);
    JSValue message = JS_GetPropertyUint32(ctx, res, 1);
    JSValue resolve = JS_GetPropertyUint32(ctx, res, 2);
    JSValue reject = JS_GetPropertyUint32(ctx, res, 3);
    if (!JS_IsNull(message)) {
        JSValue error = JS_NewError(ctx);
        JS_SetPropertyStr(ctx, error, "message", message);
        JSValueConst p[1] = {error};
        JS_Call(ctx, reject, JS_UNDEFINED, 1, p);
        JS_FreeValue(ctx, error);
    } else {
        JSValueConst p[1] = {data};
        JS_Call(ctx, resolve, JS_UNDEFINED, 1, p);
    }
    cjs->gc.erase(JS_VALUE_GET_PTR(resolve));
    cjs->gc.erase(JS_VALUE_GET_PTR(reject));
    JS_FreeValue(ctx, data);
    JS_FreeValue(ctx, message);
    JS_FreeValue(ctx, resolve);
    JS_FreeValue(ctx, reject);
}

static JSValue Context_TakeResult(JSContext *ctx) {
    bool isErr = false;
    Cjs *cjs = GetCjs(JS_GetRuntime(ctx));
    JNIEnv *env = cjs->env;
    JSValue C = GetCLib(ctx);
    JSValue apt = JS_GetPropertyStr(ctx, C, "Apt");
    JSValue loopTake = JS_GetPropertyStr(ctx, apt, "loopTake");
    JSValue res = JS_Call(ctx, loopTake, apt, 0, nullptr);
    ParseAndExecuteApt(ctx,res);
    if (CheckJvmError(env,ctx)) {
        isErr = true;
    }
    JS_FreeValue(ctx,res);
    JS_FreeValue(ctx,loopTake);
    JS_FreeValue(ctx,apt);
    JS_FreeValue(ctx,C);
    return isErr ? JS_EXCEPTION : JS_UNDEFINED;
}

static JSValue Execute_Await(JSContext *ctx, JSValue obj) {
    if(JS_HasException(ctx)){
        return JS_EXCEPTION;
    }
    Cjs *cjs = GetCjs(JS_GetRuntime(ctx));
    JSValue ret;
    int state;
    for (;;) {
        state = JS_PromiseState(ctx, obj);
        if (cjs->closed) {
            ret = JS_ThrowInterrupted(ctx);
            JS_FreeValue(ctx, obj);
            break;
        } else if (state == JS_PROMISE_FULFILLED) {
            ret = JS_PromiseResult(ctx, obj);
            JS_FreeValue(ctx, obj);
            break;
        } else if (state == JS_PROMISE_REJECTED) {
            ret = JS_Throw(ctx, JS_PromiseResult(ctx, obj));
            JS_FreeValue(ctx, obj);
            break;
        } else if (state == JS_PROMISE_PENDING) {
            if (JS_ExecutePendingJob(JS_GetRuntime(ctx), NULL) != 0) {
                continue;
            }
            JSValue val = Context_TakeResult(ctx);
            if (JS_IsUndefined(val)) {
                continue;
            }
            JS_FreeValue(ctx, obj);
            ret = val;
            break;
        } else {
            /* not a promise */
            ret = obj;
            break;
        }
    }
    CancelAllGc(cjs, ctx);
    return ret;
}

static JSValue ExecuteCmdline(JSContext *ctx, const char *cmdline) {
    JSValue val = JS_Eval(ctx, cmdline, strlen(cmdline), "<cmdline>", JS_EVAL_TYPE_GLOBAL);
    if (JS_IsException(val)) {
        return val;
    }
    val = Execute_Await(ctx, val);
    if (JS_IsException(val)) {
        return val;
    }
    return val;
}

static void ParseStack(Cjs *cjs, const char *stack, std::string &out) {
    std::regex pattern(R"(\s+(\w+)\s+\((\w+):(\d+):\d+\))");
    std::istringstream ss(stack);
    std::string line;
    while (std::getline(ss, line)) {
        std::smatch match;
        if (std::regex_search(line, match, pattern)) {
            std::string funcName = match[1].str();
            std::string source = match[2].str();
            std::string lineNum = match[3].str();
            std::string lineNum2 = "";
            try {
                if (cjs->fileName == source && cjs->lineNumber > 0) {
                    int num = atoi(lineNum.c_str());
                    lineNum2 = std::to_string(num + cjs->lineNumber - 1);
                } else {
                    lineNum2 = lineNum;
                }
            } catch (const std::exception &e) {

            }
            out.append("at ")
               .append(funcName.c_str())
               .append("(")
               .append(source.c_str())
               .append(".js")
               .append(":")
               .append(lineNum2)
               .append(")\n");
        }
    }
}

static void GetScriptError(JSContext *ctx, JSValue exception, ErrInfo &errInfo) {
    Cjs *cjs = GetCjs(JS_GetRuntime(ctx));
    JSValue jsMsg = JS_GetPropertyStr(ctx, exception, "message");
    JSValue jsStack = JS_GetPropertyStr(ctx, exception, "stack");
    errInfo.code = "1";
    if (JS_IsString(jsMsg)) {
        const char *msg = JS_ToCString(ctx, jsMsg);
        errInfo.message = msg;
        JS_FreeCString(ctx, msg);
    }
    if (JS_IsString(jsStack)) {
        const char *stack = JS_ToCString(ctx, jsStack);
        std::string out;
        ParseStack(cjs, stack, out);
        errInfo.traceback = out;
        JS_FreeCString(ctx, stack);
    }
    JS_FreeValue(ctx, jsStack);
    JS_FreeValue(ctx, jsMsg);
}

static void ThrowJvmError(JNIEnv *env, JSContext *ctx, JSValue exception) {
    ErrInfo errInfo;
    GetScriptError(ctx, exception, errInfo);
    JBootstrap boot(env);
    JLocal<jobject> excRef(env, NewScriptException(env,
                                             errInfo.code.c_str(),
                                             errInfo.message.c_str(),
                                             errInfo.traceback.c_str()));
    env->Throw(static_cast<jthrowable>(excRef.get()));
}

static bool CheckAndThrowJvmError(JNIEnv *env, JSContext *ctx){
    if(JS_HasException(ctx)){
        JSValue err = JS_GetException(ctx);
        ThrowJvmError(env,ctx, err);
        JS_FreeValue(ctx,err);
        return true;
    }
    return false;
}

static void AptGc(JSRuntime *rt, JSValue val) {
    Cjs *cjs = GetCjs(rt);
    JNIEnv *env = cjs->env;
    jobject context = cjs->context;
    JSClassID aptClass = cjs->aptClass;
    Apt *apt = static_cast<Apt *>(JS_GetOpaque(val, aptClass));
    env->CallIntMethod(apt->object, apt->gcMet, context);
    for (int i = 0; i < apt->len; i++) {
        env->DeleteGlobalRef(apt->methods[i]);
        free(apt->signs[i]);
    }
    delete[] apt->methods;
    delete[] apt->signs;
    env->DeleteGlobalRef(apt->object);
    free(apt);
}

static JSClassDef AptObjectClass = {
        .class_name = "Apt",
        .finalizer = AptGc
};

static JSClassID CreateAptClass(JSRuntime *rt) {
    JSClassID aptClassId = 0;
    JS_NewClassID(&aptClassId);
    JS_NewClass(rt, aptClassId, &AptObjectClass);
    return aptClassId;
}
static class Js2JRet{
    JSContext *ctx;
    JNIEnv *env;
public:
    Js2JRet(JSContext *ctx,JNIEnv *env) : env(env), ctx(ctx) {
    }

    jobject newBool(JSValueConst arg){
        return NewBool(env,JS_ToBool(ctx,arg));
    }
    jobject newString(JSValueConst arg){
        const char* r = JS_ToCString(ctx,arg);
        jobject ret = NewString(env,r);
        JS_FreeCString(ctx,r);
        return ret;
    }
    jobject newNumber(JSValueConst arg){
        if(JS_VALUE_GET_TAG(arg)==JS_TAG_INT) {
            jlong value = 0;
            JS_ToInt64(ctx, &value, arg);
            return NewLong(env,value);
        }else{
            jdouble value = 0;
            JS_ToFloat64(ctx, &value, arg);
            return NewDouble(env,value);
        }
    }

    jobject newAny(JSValueConst arg){
        if(JS_IsNumber(arg)) {
            return this->newNumber(arg);
        }else if(JS_IsBool(arg)){
            return this->newBool(arg);
        }else if(JS_IsString(arg)){
            return this->newString(arg);
        }else{
            return nullptr;
        }
    }
};

static inline JS_BOOL JS_IS_ArrayBuffer(JSContext *ctx,JSValueConst value)
{
    if (!JS_IsObject(value)) {
        return true;
    }
    JSValue global_obj = JS_GetGlobalObject(ctx);
    JSValue constructor = JS_GetPropertyStr(ctx, global_obj, "ArrayBuffer");
    bool result = JS_IsInstanceOf(ctx, value, constructor);
    JS_FreeValue(ctx, constructor);
    JS_FreeValue(ctx, global_obj);
    return !result;
}

static inline JS_BOOL JS_IS_APT(JSContext *ctx, JSValueConst value) {
    Cjs *cjs = GetCjs(JS_GetRuntime(ctx));
    if (JS_GetClassID(value) != cjs->aptClass) {
        return true;
    }
    return false;
}

static class JS2JArgs {
private:
    int idx = 0;
    int pos = 0;
    JSContext *ctx;
    JNIEnv *env;
    JSValueConst *argv;
    jobjectArray array;
public:
    JS2JArgs(JSContext *ctx, JNIEnv *env, jobjectArray array, JSValueConst *argv,int pos=0) : ctx(ctx), env(env), array(array), argv(argv),pos(pos) {
    }
    void throwRuntimeError(const char *type){
        JS_ThrowRuntimeError(ctx, "The %d expectation is a %s value!", pos + 1,type);
    }
    bool checkType(JS_BOOL(*func)(JSValueConst), const char *type){
        if (!func(argv[pos])) {
            throwRuntimeError(type);
            return true;
        }
        return false;
    }

    bool setMap(){
        if(checkType(JS_IsObject,"object")){
            return true;
        }
        JSValue arg = argv[pos];
        JLocal<jobject> _map(env, NewMap(env));
        JMap map(env,_map.get());
        JSPropertyEnum *tab;
        uint32_t len;
        JS_GetOwnPropertyNames(ctx, &tab, &len, arg, JS_GPN_ENUM_ONLY | JS_GPN_STRING_MASK);
        for (int i = 0; i < len; i++) {
            JSAtom atom = tab[i].atom;
            size_t cLen;
            const char * tKey = JS_AtomToCStringLen(ctx, &cLen, atom);
            JSValue value = JS_GetProperty(ctx,arg,atom);
            JLocal<jobject> ret(env,Js2JRet(ctx,env).newAny(value));
            JLocal<jobject> key(env, NewString(env,tKey));
            map.put(key.get(),ret.get());
            JS_FreeCString(ctx, tKey);
            JS_FreeValue(ctx, value);
        }
        env->SetObjectArrayElement(array, idx, _map.get());
        JS_FreePropertyEnum(ctx, tab, len);
        return false;
    }
    bool setCallback(){
        JSValue item = argv[pos];
        if(!JS_IsFunction(ctx,item)){
            throwRuntimeError("callback");
            return true;
        }
        void* hold = JS_VALUE_GET_PTR(item);
        Cjs *cjs = GetCjs(JS_GetRuntime(ctx));
        JLocal<jobject> ret(env, NewCallback(env,cjs->context,reinterpret_cast<jlong>(hold)));
        env->SetObjectArrayElement(array, idx, ret.get());
        return false;
    }
    bool setBytes(){
        if(JS_IS_ArrayBuffer(ctx,argv[pos])){
            throwRuntimeError("ArrayBuffer");
            return true;
        }
        size_t len;
        const char* bytes = reinterpret_cast<const char *>(JS_GetArrayBuffer(ctx, &len, argv[pos]));
        JLocal<jobject> ret(env, NewBytes(env, (jbyte *) bytes, len));
        env->SetObjectArrayElement(array, idx, ret.get());
        return false;
    }

    bool setNumber(){
        if(checkType(JS_IsNumber,"number")){
            return true;
        }
        JSValueConst arg = argv[pos];
        if(JS_VALUE_GET_TAG(argv[pos])==JS_TAG_INT) {
            jlong value = 0;
            JS_ToInt64(ctx, &value, arg);
            JLocal<jobject> data(env, NewLong(env, value));
            env->SetObjectArrayElement(array, idx, data.get());
        }else{
            JSValueConst arg = argv[pos];
            jdouble value = 0;
            JS_ToFloat64(ctx, &value, arg);
            JLocal<jobject> data(env, NewDouble(env, value));
            env->SetObjectArrayElement(array, idx, data.get());
        }
        return false;
    }

    bool setBool() {
        if(checkType(JS_IsBool,"bool")){
            return true;
        }
        JSValueConst arg = argv[pos];
        jboolean value = JS_ToBool(ctx, arg);
        JLocal<jobject> data(env, NewBool(env, value));
        env->SetObjectArrayElement(array, idx, data.get());
        return false;
    }

    bool setString() {
        if(checkType(JS_IsString,"string")){
            return true;
        }
        JSValueConst arg = argv[pos];
        const char *str = JS_ToCString(ctx, arg);
        JLocal<jobject> data(env, NewString(env, str));
        env->SetObjectArrayElement(array, idx, data.get());
        JS_FreeCString(ctx, str);
        return false;
    }

    bool setObjApt() {
        if(JS_IS_APT(ctx,argv[pos])){
            throwRuntimeError("apt");
            return true;
        }
        JSValueConst arg = argv[pos];
        Apt *apt = (Apt *) JS_GetOpaque(arg, GetCjs(JS_GetRuntime(ctx))->aptClass);
        env->SetObjectArrayElement(array, idx, apt->object);
        return false;
    }

    bool setFunction(){
        JSValueConst arg = argv[pos];
        JSValue toString = JS_GetPropertyStr(ctx, arg, "toString");
        JSValue funcBody = JS_Call(ctx, toString, arg, 0, NULL);
        JSValue fileName = JS_GetPropertyStr(ctx, arg, "fileName");
        JSValue lineNumber = JS_GetPropertyStr(ctx, arg, "lineNumber");
        jint lineNumberInt = 0;
        const char *fileNameStr = JS_ToCString(ctx,fileName);
        const char *codeStr = JS_ToCString(ctx,funcBody);
        JS_ToInt32(ctx,&lineNumberInt,lineNumber);
        size_t len = strlen(codeStr);
        JLocal<jbyteArray> codeJ(env,env->NewByteArray(len));
        env->SetByteArrayRegion(codeJ.get(), 0, len, (jbyte*)codeStr);
        JLocal<jobject> fileNameJ(env, NewString(env,fileNameStr));
        JLocal<jobject> lineNumberJ(env, NewInt(env,lineNumberInt));
        JLocal<jobject> func(env,NewFunction(env,codeJ.get(),fileNameJ.get(),lineNumberJ.get()));
        env->SetObjectArrayElement(array, idx, func.get());
        JS_FreeCString(ctx,fileNameStr);
        JS_FreeCString(ctx,codeStr);
        JS_FreeValue(ctx,funcBody);
        JS_FreeValue(ctx,toString);
        JS_FreeValue(ctx,fileName);
        JS_FreeValue(ctx,lineNumber);
        return false;
    }

    bool setObjectPtr(){
        Cjs *cjs = GetCjs(JS_GetRuntime(ctx));
        JSValueConst arg = argv[pos];
        void* ptr = JS_VALUE_GET_PTR(arg);
        JLocal<jobject> data(env,NewObjectPtr(env,ptr));
        env->SetObjectArrayElement(array, idx, data.get());
        return false;
    }

    bool setAny(){
        if(JS_IsNumber(argv[pos])){
            return this->setNumber();
        }else if(JS_IsBool(argv[pos])){
            return this->setBool();
        }else if(JS_IsString(argv[pos])){
            return this->setString();
        }else if(JS_IsFunction(ctx,argv[pos])){
            //todo
            //return this->setFunction();
            return this->setCallback();
        }
        return false;
    }

    bool setVarargs(size_t cnt){
        int len = cnt - pos;
        JLocal<jobjectArray> args(env, NewObjectArray(env, len));
        JS2JArgs myL2j(ctx, env, args.get(), argv, pos);
        for (int i = 0; i < len; i++) {
            myL2j.setAny();
            myL2j.next();
        }
        JLocal<jobject> varargs(env, NewVarargs(env,args.get()));
        env->SetObjectArrayElement(array, pos, varargs.get());
        return false;
    }

    void next() {
        pos++;
        idx++;
    }


};

static bool JsToJava(JSContext *ctx, JNIEnv *env, const char *buf, jobjectArray array, int argc, JSValueConst *argv) {
    JS2JArgs js2J(ctx,env,array,argv);
    for (;;) {
        if ((*(buf)) == '\0') {
            break;
        }
        if ((*buf) == ENGINE_TYPE_NUMBER) {
            if(js2J.setNumber()){
                return true;
            }
        } else if ((*buf) == ENGINE_TYPE_MAP) {
            if(js2J.setMap()){
                return true;
            }
        } else if ((*buf) == ENGINE_TYPE_BOOLEAN) {
            if(js2J.setBool()){
                return true;
            }
        } else if ((*buf) == ENGINE_TYPE_STRING) {
            if(js2J.setString()){
                return true;
            }
        } else if ((*buf) == ENGINE_TYPE_OBJ_APT) {
            if(js2J.setObjApt()){
                return true;
            }
        } else if ((*buf) == ENGINE_TYPE_FUNCTION) {
            if(js2J.setFunction()){
                return true;
            }
        } else if ((*buf) == ENGINE_TYPE_ANY) {
            if(js2J.setAny()){
                return true;
            }
        } else if ((*buf) == ENGINE_TYPE_OBJ_PTR) {
            if(js2J.setObjectPtr()){
                return true;
            }
        } else if ((*buf) == ENGINE_TYPE_VARARGS) {
            if(js2J.setVarargs(argc)){
                return true;
            }
            break;
        } else if ((*buf) == ENGINE_TYPE_BYTES) {
            if(js2J.setBytes()){
                return true;
            }
        } else if ((*buf) == ENGINE_TYPE_CALLBACK) {
            if(js2J.setCallback()){
                return true;
            }
        }
        js2J.next();
        buf++;
    }
    return false;
}

static char GetLstChar(const char *argSign){
    if (argSign == nullptr || strlen(argSign) == 0) {
        return '\0';
    }
    size_t len = strlen(argSign);
    char lastChar = argSign[len - 1];
    return lastChar;
}

static jobjectArray BuildInvokeParam(JSContext *ctx, JNIEnv *env, const char *argSign, int jsLen, JSValueConst *argv) {
    int jaLen = strlen(argSign);
    if (jsLen < jaLen && !((jsLen + 1 == jaLen) && GetLstChar(argSign) == ENGINE_TYPE_VARARGS)) {
        JS_ThrowRuntimeError(ctx, "Requires %d parameters!", jaLen);
        return nullptr;
    }
    jobjectArray param = NewObjectArray(env, jaLen);
    if (JsToJava(ctx, env, argSign, param, jsLen, argv)) {
        return nullptr;
    }
    return param;
}

static JSValue RedirectCallerObj(JSContext *ctx, JSValueConst this_val, int argc, JSValueConst *argv, int magic) {
    JSRuntime *rt = JS_GetRuntime(ctx);
    Cjs *cjs = GetCjs(rt);
    if(cjs->closed){
        return JS_ThrowInterrupted(ctx);
    }
    Apt *apt = (Apt *) JS_GetOpaque(this_val, cjs->aptClass);
    if (!apt) {
        return JS_ThrowRuntimeError(ctx, "Call failed!");
    }
    JNIEnv *env = cjs->env;
    jobject context = cjs->context;
    jobject method = apt->methods[magic];
    char *sign = apt->signs[magic];
    char retType = sign[0];
    const char *argSign = sign + 1;
    jobjectArray params = BuildInvokeParam(ctx, env, argSign, argc, argv);
    if (JS_HasException(ctx)) {
        return JS_EXCEPTION;
    }
    JLocal<jobjectArray> arg(env, params);
    jmethodID invoke = apt->invokeMet;
    jvalue param[3];
    param[0].l = context;
    param[1].l = method;
    param[2].l = arg.get();
    JLocal<jobject> data(env, env->CallObjectMethodA(apt->object, invoke, param));
    if(CheckJvmError(env,ctx)){
        return JS_EXCEPTION;
    }
    if (data.get() != nullptr && IsObjApt(env, data.get())) {
        JLocal<jobjectArray> info(env, UnpackJniBaseApt(env, data.get()));
        JLocal<jobject> apt2(env, JArray(env, info.get()).get(0));
        if (env->IsSameObject(apt->object, apt2.get())) {
            return JS_DupValue(ctx,this_val);
        }
    }
    J2JS j2Js(ctx, env);
    JSValue ret = j2Js.newAny(data.get());
    return ret;
}

static JSValue RedirectCallerMet(JSContext *ctx, JSValueConst this_val, int argc, JSValueConst *argv, int magic, JSValue *func_data) {
    JSValue caller = func_data[0];
    return RedirectCallerObj(ctx, caller, argc, argv, 0);
}

Cjs::Cjs(JNIEnv *env, jobject context, jbyteArray _zip) : env(env) {
    JBytes zip = JBytes(env, _zip);
    JSRuntime *rt = JS_NewRuntime();
    JBootstrap boot(env);
    JSContext *ctx = JsNewCustomContext(rt);
    JS_SetModuleLoaderFunc(rt, nullptr, JsModuleLoader, nullptr);
    JS_SetHostPromiseRejectionTracker(rt, JsStdPromiseRejectionTracker, NULL);
    JS_SetInterruptHandler(rt, JsInterruptHandler, NULL);
    JS_SetRuntimeOpaque(rt, this);
    this->zipLen = zip.size();
    this->zip = static_cast<const char *>(memcpy(new char[zip.size()], zip.str(), zip.size()));
    this->aptClass = CreateAptClass(rt);
    this->context = env->NewGlobalRef(context);
    this->bitmap = (Bitmap *) malloc(sizeof(Bitmap));
    this->rt = rt;
    this->ctx = ctx;
}

Cjs::~Cjs() {
    //js_std_free_handlers(this->rt);
    JS_FreeContext(this->ctx);
    JS_FreeRuntime(this->rt);
    free(this->bitmap);
    this->env->DeleteGlobalRef(this->context);
    if (this->zipLen > 0) {
        delete this->zip;
    }
}

void Cjs::interrupt() {
    this->closed = true;
}

void Cjs::setInteger(jstring key, jlong val) {
    JSValue C = GetCLib(this->ctx);
    JString jvKey = JString(this->env, key);
    JSValue jsVal = JS_NewInt64(ctx, val);
    JS_SetPropertyStr(ctx, C, jvKey.str(), jsVal);
    JS_FreeValue(ctx, jsVal);
    JS_FreeValue(ctx, C);
}

void Cjs::setNumber(jstring key, jdouble val) {
    JSValue C = GetCLib(this->ctx);
    JString jvKey = JString(this->env, key);
    JSValue jsVal = JS_NewFloat64(ctx, val);
    JS_SetPropertyStr(ctx, C, jvKey.str(), jsVal);
    JS_FreeValue(ctx, jsVal);
    JS_FreeValue(ctx, C);
}

void Cjs::setString(jstring key, jstring val) {
    JSValue C = GetCLib(this->ctx);
    JString jvKey = JString(this->env, key);
    JString jvVal = JString(this->env, val);
    JSValue jsVal = JS_NewString(ctx, jvVal.str());
    JS_SetPropertyStr(ctx, C, jvKey.str(), jsVal);
    JS_FreeValue(ctx, jsVal);
    JS_FreeValue(ctx, C);
}

void Cjs::setBytes(jstring key, jbyteArray val) {
    JSValue C = GetCLib(this->ctx);
    JString jvKey = JString(this->env, key);
    JBytes jvVal = JBytes(this->env, val);
    JSValue jsVal = JS_NewArrayBufferCopy(ctx, (unsigned char *) jvVal.str(), jvVal.size());
    JS_SetPropertyStr(ctx, C, jvKey.str(), jsVal);
    JS_FreeValue(ctx, jsVal);
    JS_FreeValue(ctx, C);
}

void Cjs::setBool(jstring key, jboolean val) {
    JSValue C = GetCLib(ctx);
    JString jvKey = JString(env, key);
    JSValue jsVal = JS_NewBool(ctx, val);
    JS_SetPropertyStr(ctx, C, jvKey.str(), jsVal);
    JS_FreeValue(ctx, jsVal);
    JS_FreeValue(ctx, C);
}

JSValue BuildExecuteFunction(JSContext *ctx,JNIEnv *env,jstring _module, jstring _func){
    JString module = JString(env, _module);
    JString func = JString(env, _func);
    std::string command;
    command.append("let __p = async function(){\n");
    command.append("let m = await import(\"").append(module.str()).append("\");\n");
    command.append("if(!m.default){")
            .append("throw new Error(\"lost default\");")
            .append("}\n");
    command.append("if(!m.default.").append(func.str()).append("){")
            .append("throw new Error(\"lost ").append(func.str()).append(" Function\");")
            .append("}\n");
    command.append("return await m.default.").append(func.str()).append("();\n");
    command.append("};\n");
    command.append("__p;");
    return JS_Eval(ctx, command.c_str(), command.size(), "<function>", JS_EVAL_TYPE_GLOBAL);
}

jobject Cjs::execute(jstring _module, jstring _func) {
    JSValue function = BuildExecuteFunction(ctx,env,_module,_func);
    JSValue promise = JS_Call(ctx,function,JS_UNDEFINED,0, nullptr);
    JS_FreeValue(ctx,function);
    JSValue ret = Execute_Await(ctx,promise);
    if(CheckAndThrowJvmError(env,ctx)){
        JS_FreeValue(ctx,ret);
        return nullptr;
    }
    Js2JRet js2J(ctx,env);
    jobject retJ = js2J.newAny(ret);
    JS_FreeValue(ctx,ret);
    return retJ;
}

jobject Cjs::executeCmdline(jstring cmdline, jobjectArray args) {
    JString cmd = JString(env, cmdline);
    JSValue ret = ExecuteCmdline(ctx,cmd.str());
    if(JS_IsException(ret)){
        JSValue err = JS_GetException(ctx);
        ThrowJvmError(env,ctx, err);
        JS_FreeValue(ctx,err);
        return nullptr;
    }
    return Js2JRet(ctx,env).newAny(ret);
}

static JSValue BuildExecuteFunction(JSContext *ctx, JNIEnv *env,const char* fileName, jbyteArray _code) {
    JBytes code = JBytes(env, _code);
    std::string body;
    body.append("let anonymous=");
    body.append(code.str(), code.size());
    body.append(";anonymous;");
    return JS_Eval(ctx, body.c_str(), body.size(), fileName, JS_EVAL_TYPE_GLOBAL);
}

jobject Cjs::executeFunction(jbyteArray code, jstring file_name, jint line_number, jobjectArray _args) {
    this->fileName.clear();
    JString fileName(env, file_name);
    this->fileName.append(fileName.str());
    this->lineNumber = line_number;
    JSValue function = BuildExecuteFunction(ctx, env, fileName.str(), code);

    JArray args(env, _args);
    JSValueConst *params = new JSValueConst[args.size()];
    for (int i = 0; i < args.size(); i++) {
        params[i] = J2JS(ctx, env).newAny(args.get(i));
    }

    JSValue promise = JS_Call(ctx, function, JS_UNDEFINED, args.size(), params);
    delete[] params;
    JS_FreeValue(ctx, function);
    JSValue ret = Execute_Await(ctx, promise);
    if (CheckAndThrowJvmError(env, ctx)) {
        JS_FreeValue(ctx, ret);
        return nullptr;
    }
    Js2JRet js2J(ctx, env);
    jobject retJ = js2J.newAny(ret);
    JS_FreeValue(ctx, ret);
    return retJ;
}

void Cjs::setObjApt(jstring key, jobject _info) {
    JSValue val = JS_DupValue(ctx, NewObjApt(env, ctx, _info));
    JSValue C = GetCLib(ctx);
    JString jvKey = JString(env, key);
    JS_SetPropertyStr(ctx, C, jvKey.str(), val);
    JS_FreeValue(ctx, val);
    JS_FreeValue(ctx, C);
}

void Cjs::setFuncApt(jstring key, jobject _info) {
    JSValue func = JS_DupValue(ctx, NewFuncApt(env, ctx, _info));
    JSValue C = GetCLib(ctx);
    JString jvKey = JString(env, key);
    JS_SetPropertyStr(ctx, C, jvKey.str(), func);
    JS_FreeValue(ctx, func);
    JS_FreeValue(ctx, C);
}

jobject Cjs::callback(jlong hold, jobjectArray _args) {
    JSValue holder = JS_MKPTR(JS_TAG_OBJECT,(void *)(hold));
    JArray args(env, _args);
    JSValueConst *params = new JSValueConst[args.size()];
    for (int i = 0; i < args.size(); i++) {
        params[i] = J2JS(ctx, env).newAny(args.get(i));
    }
    JSValue ret = JS_Call(ctx,holder, JS_UNDEFINED,args.size(), params);
    Js2JRet js2J(ctx,env);
    jobject retJ = js2J.newAny(ret);
    JS_FreeValue(ctx,ret);
    delete[] params;
    return retJ;
}

void Cjs::setBitmap(jint width, jint height, jint row_stride, jint pixel_stride, jobject buff) {
    this->bitmap->width = width;
    this->bitmap->height = height;
    this->bitmap->rowShift = row_stride;
    this->bitmap->pixelStride = pixel_stride;
    this->bitmap->origin = (unsigned char *) env->GetDirectBufferAddress(buff);
}

Cjs *openCjs(JNIEnv *env, jobject context, jbyteArray _zip) {
    return new Cjs(env, context, _zip);
}

Cjs *toCjs(jlong ptr) {
    return ((Cjs *) ptr);
}

void closeCjs(Cjs *cjs) {
    delete cjs;
}