//
// Created by 86152 on 2025/12/11.
//

#ifndef CROBOT_CJS_H
#define CROBOT_CJS_H

#include <jni.h>
#include <string>
#include <unordered_set>
#include "../quickjs/quickjs.h"
#include "../core/image.h"
#define ENGINE_TYPE_ANY '1'
#define ENGINE_TYPE_NUMBER '2'
#define ENGINE_TYPE_BOOLEAN '3'
#define ENGINE_TYPE_STRING '4'
#define ENGINE_TYPE_OBJ_APT '5'
#define ENGINE_TYPE_FUNCTION '6'
#define ENGINE_TYPE_VARARGS '7'
#define ENGINE_TYPE_OBJ_PTR '8'
#define ENGINE_TYPE_MAP '9'
#define ENGINE_TYPE_BYTES 'a'
#define ENGINE_TYPE_CALLBACK 'b'
class Cjs {
public:
    JNIEnv *env;
    JSRuntime *rt;
    JSContext *ctx;
    jobject context;
    bool closed = false;
    const char *zip;
    size_t zipLen=0;
    JSClassID aptClass;
    std::string fileName;
    int lineNumber = -1;
    Bitmap* bitmap;
    std::unordered_set<void*> gc;
    Cjs(JNIEnv *env, jobject context,jbyteArray zip);
    ~Cjs();
    void interrupt();
    void setInteger(jstring key, jlong val);
    void setNumber(jstring key, jdouble val);
    void setString(jstring key, jstring val);
    void setBytes(jstring key, jbyteArray val);
    void setBool(jstring key, jboolean val);
    jobject execute(jstring module, jstring func);
    jobject executeCmdline(jstring cmdline, jobjectArray args);
    jobject executeFunction(jbyteArray code, jstring file_name, jint line_number, jobjectArray args);
    void setObjApt(jstring key, jobject val);
    void setFuncApt(jstring key, jobject val);
    void setBitmap(jint width, jint height, jint row_stride, jint pixel_stride, jobject buff);
    jobject callback(jlong i, jobjectArray pArray);
};

Cjs *openCjs(JNIEnv *env,jobject context,jbyteArray _zip);
Cjs *toCjs(jlong ptr);
void closeCjs(Cjs *cjs);


#endif //CROBOT_CJS_H
