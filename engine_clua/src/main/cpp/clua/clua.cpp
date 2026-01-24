//
// Created by 86152 on 2025/12/14.
//

#include "clua.h"
#include "../core/jvm.h"
#include "../core/bootstrap.h"
#include "../core/zip.h"
#include <string>
#include <regex>
#include <iostream>
#include <sstream>

typedef struct {
    jobject object;
    jobject *methods;
    int methodLen;
} Apt;

typedef struct {
    std::string code;
    std::string message;
    std::string traceback;
} ErrInfo;

static CLua *GetCLua(lua_State *L) {
    return (*((CLua **) lua_getextraspace(L)));
}

static void LuaInterruptHandler(lua_State *L, lua_Debug *ar) {
    if (ar->event == LUA_HOOKLINE) {
        CLua *cLua = GetCLua(L);
        if (cLua->closed) {
            luaL_error(L, "program thread interruption!");
        }
    }
}

static void GetCLib(lua_State *L) {
    lua_pushglobaltable(L);
    //lua_getglobal(L, C_ROBOT_LIB);
}

static int CallSetCLib(lua_State *L) {
    GetCLib(L);
    lua_insert(L, 1);
    lua_settable(L, -3);
    return 0;
}

void static SetCLib(lua_State *L, const char *lib_name) {
    int valuePos = lua_gettop(L);
    lua_pushcfunction(L, CallSetCLib);
    lua_pushstring(L, lib_name);         //Key   1
    lua_pushvalue(L, valuePos);        //Value  2
    lua_pcall(L, 2, 0, 0);
    lua_pop(L, 1);
}

static void CreateCLib(lua_State *L) {
    lua_createtable(L, 0, 0);
    lua_setglobal(L, C_ROBOT_LIB);
}
static bool CheckLuaException(lua_State *L, int code, ErrInfo &err);
static void ThrowJvmError(JNIEnv *env, ErrInfo &errInfo);
static void UpdateImageIfNeed(lua_State *L){
    JNIEnv *env = GetCLua(L)->env;
    int l = lua_gettop(L);
    GetCLib(L);
    lua_getfield(L, -1, "Display");
    lua_getfield(L, -1, "updateIfNeed");
    lua_pushvalue(L,-2);
    ErrInfo errInfo;
    int code = lua_pcall(L, 1, 0, 0);
    if (CheckLuaException(L, code, errInfo)) {
        ThrowJvmError(env, errInfo);
    }
    lua_settop(L,l);
}


static void CreateImageLib(lua_State *L){
    #define METHOD(f) {#f,f}
    auto getPixelColor = [](lua_State *L) -> int {
        luaL_checktype(L, 1, LUA_TTABLE);
        int x = luaL_checkinteger(L, 2);
        int y = luaL_checkinteger(L, 3);
        UpdateImageIfNeed(L);

        Bitmap* bitmap = GetCLua(L)->bitmap;
        Color out;
        GetSimpleColor(bitmap, x, y, &out);
        lua_pushinteger(L, out.hex);
        return 1;
    };
    auto findColor = [](lua_State *L) -> int {
        luaL_checktype(L, 1, LUA_TTABLE);
        int x = luaL_checkinteger(L, 2);
        int y = luaL_checkinteger(L, 3);
        int x1 = luaL_checkinteger(L, 4);
        int y1 = luaL_checkinteger(L, 5);
        const char *str = luaL_checkstring(L, 6);
        int dir = luaL_checkinteger(L, 7);
        double sim = luaL_checknumber(L, 8);
        UpdateImageIfNeed(L);

        Bitmap* bitmap = GetCLua(L)->bitmap;
        Point out;
        FindColor(bitmap, x, y, x1, y1,str,sim,dir,&out);
        lua_pushinteger(L, out.x);
        lua_pushinteger(L, out.y);
        return 2;
    };
    auto findMultiColor = [](lua_State *L) -> int {
        luaL_checktype(L, 1, LUA_TTABLE);
        int x = luaL_checkinteger(L, 2);
        int y = luaL_checkinteger(L, 3);
        int x1 = luaL_checkinteger(L, 4);
        int y1 = luaL_checkinteger(L, 5);
        const char *first = lua_tostring(L, 6);
        const char *featureString = luaL_checkstring(L, 7);
        int dir = luaL_checkinteger(L, 8);
        double sim = luaL_checknumber(L, 9);
        UpdateImageIfNeed(L);
        Bitmap* bitmap = GetCLua(L)->bitmap;
        Point point;
        FindFeature(bitmap, x, y, x1, y1, first, featureString, sim, dir, &point);
        lua_pushinteger(L, point.x);
        lua_pushinteger(L, point.y);
        return 2;
    };
    auto findComplexMultiColor = [](lua_State *L) -> int {
        luaL_checktype(L, 1, LUA_TTABLE);

        int x = luaL_checkinteger(L, 2);
        int y = luaL_checkinteger(L, 3);
        int x1 = luaL_checkinteger(L, 4);
        int y1 = luaL_checkinteger(L, 5);
        const char *first = lua_tostring(L, 6);
        const char *featureString = luaL_checkstring(L, 7);
        const char *noFeatureString = luaL_checkstring(L, 8);
        int dir = luaL_checkinteger(L, 9);
        double sim = luaL_checknumber(L, 10);
        double tolerance = luaL_checknumber(L, 11);
        UpdateImageIfNeed(L);
        Bitmap* bitmap = GetCLua(L)->bitmap;
        Point out;
        FindComplexFeature(bitmap, x, y, x1, y1, first, featureString, noFeatureString, sim, tolerance, dir, &out);
        lua_pushinteger(L, out.x);
        lua_pushinteger(L, out.y);
        return 2;
    };
    auto cmpColor = [](lua_State *L) -> int {
        luaL_checktype(L, 1, LUA_TTABLE);
        int x = luaL_checkinteger(L, 2);
        int y = luaL_checkinteger(L, 3);
        const char *str = lua_tostring(L, 4);
        double sim = lua_tonumber(L, 5);
        UpdateImageIfNeed(L);
        Bitmap* bitmap = GetCLua(L)->bitmap;
        lua_pushboolean(L, CmpColor(bitmap, x, y, str, sim));
        return 1;
    };
    static luaL_Reg methods[] = {
            METHOD(getPixelColor),
            METHOD(findColor),
            METHOD(findMultiColor),
            METHOD(findComplexMultiColor),
            METHOD(cmpColor),
            {nullptr, nullptr}
    };
    luaL_newlib(L, methods);
    SetCLib(L, "Screen");
}

static void GetCLib(lua_State *L, const char *name) {
    GetCLib(L);
    lua_pushstring(L, name);
    lua_rawget(L, -2);
}

int CallPrintLib(lua_State *L, int level, const char *tag, const char *message) {
    lua_Debug debug;
    lua_getstack(L, level, &debug);
    lua_getinfo(L, "Sln", &debug);
    const char *source = debug.source;
    int currentLine = debug.currentline;
    GetCLib(L, "output");
    lua_pushstring(L, source);
    lua_pushnumber(L, currentLine);
    lua_pushstring(L, message);
    lua_pushstring(L, tag);
    lua_pcall(L, 4, 0, 0);
    return 0;
}

int CallDebugPrint(lua_State *L) {
    int level = 1;
    const char *tag = "INFO";
    level = lua_type(L, 2) == LUA_TNUMBER ? lua_tonumber(L, 2) : level;
    tag = lua_type(L, 3) == LUA_TSTRING ? lua_tostring(L, 3) : tag;
    lua_pushvalue(L, lua_upvalueindex(1));
    lua_pushvalue(L, 1);
    lua_pcall(L, 1, 1, 0);
    const char *message = lua_tostring(L, -1);
    CallPrintLib(L, level, tag, message);
    return 0;
}

void ParseTraceback(const char *traceback, std::string &out) {
    std::regex pattern("\\[string \"([^\"]+)\"\\]:(\\d+): in function (['<])([^'>]+)['>]", std::regex::ECMAScript);
    std::istringstream ss(traceback);
    std::string line;
    while (std::getline(ss, line)) {
        std::smatch match;
        if (std::regex_search(line, match, pattern)) {
            std::string source = match[1].str();
            std::string lineNum = match[2].str();
            std::string wrapper = match[3].str();
            std::string funcInfo;
            if (wrapper == "<") {
                funcInfo = "anonymous";
            } else {
                funcInfo = match[4].str();
            }
            out.append("in function ")
               .append(funcInfo)
               .append("(")
               .append(source.c_str())
               .append(":")
               .append(lineNum)
               .append(")\n");
        }
    }
}

void ParseMessage(const char *message, std::string &out) {
    std::string text = message;
    size_t lstPos = text.find_last_of(':');
    if (lstPos == std::string::npos) {
        out.append(message);
        return;
    }
    size_t startPos = lstPos + 1;
    while (startPos < text.size() && isspace(static_cast<unsigned char>(text[startPos]))) {
        startPos++;
    }
    if (startPos >= text.size()) {
        return;
    }
    out = text.substr(startPos);
}


static bool CheckLuaException(lua_State *L, int code, ErrInfo &err) {
    if (code == LUA_OK) {
        err.code = "0";
        return false;
    }
    CLua *cLua = GetCLua(L);
    if (cLua->closed == 1) {
        err.code = "1";
    }
    if (lua_isstring(L, -1)) {
        err.code = std::to_string(code);
        return true;
    }
    if (lua_istable(L, -1)) {
        int tablePos = lua_gettop(L);
        std::string message;
        std::string traceback;
        lua_rawgeti(L, tablePos, 3);
        ParseMessage(lua_tostring(L, -1), message);
        lua_rawgeti(L, tablePos, 4);
        ParseTraceback(lua_tostring(L, -1), traceback);
        err.code = code;
        err.message = message;
        err.traceback = traceback;
        lua_settop(L, tablePos);
    }
    return true;
}

void CreatePrintLib(lua_State *L) {
    char *command = R"===(
    local function serialize(obj, base_indent, visited)
        base_indent = base_indent or ""
        visited = visited or {}
        local buffer = {}
        local function add(str)
            table.insert(buffer, str)
        end
        local obj_type = type(obj)
        if obj_type == "nil" then
            add("nil")
        elseif obj_type == "boolean" then
            add(tostring(obj))
        elseif obj_type == "number" then
            add(tostring(obj))
        elseif obj_type == "string" then
            local escaped_str = obj:gsub('"', '\\"'):gsub('\n', '\\n')
            add('"' .. escaped_str .. '"')
        elseif obj_type == "function" then
            add(tostring(obj))
        elseif obj_type == "userdata" then
            add(tostring(obj))
        elseif obj_type == "thread" then
            add(tostring(obj))
        elseif obj_type == "table" then
            if visited[obj] then
                add(tostring(obj) .. " (循环引用)")
                return table.concat(buffer)
            end
            visited[obj] = true
            add(tostring(obj) .. " {")
            add("\n")
            local inner_indent = base_indent .. "  "
            local has_items = false
            for k, v in pairs(obj) do
                has_items = true
                add(inner_indent)
                add("[")
                add(type(k) == "table" and serialize(k, "", visited) or tostring(k))
                add("] = ")
                add(serialize(v, inner_indent, visited))
                add("\n")
            end
            if has_items then
                add(base_indent)
            end
            add("}")
            visited[obj] = nil
        end
        return table.concat(buffer)
    end
    return serialize;
    )===";
    ErrInfo err;
    int code = luaL_loadbufferx(L, command, strlen(command), "<debug>", "t");
    CheckLuaException(L,code,err);
    code = lua_pcall(L, 0, 1, 0);
    CheckLuaException(L,code,err);
    lua_pushcclosure(L, CallDebugPrint,1);
    lua_setglobal(L, "print");
}

static int PushException(lua_State *L, const char *message) {
    lua_Debug debug;
    for (int i = 1; i <= 20; i++) {
        lua_getstack(L, i, &debug);
        lua_getinfo(L, "Sln", &debug);
        if (strcmp("C", debug.what) < 0) {
            break;
        }
    }
    if (-1 == debug.currentline) {
        lua_pushstring(L, message);
        return 1;
    }
    lua_createtable(L, 3, 0);
    lua_pushstring(L, debug.source);
    lua_rawseti(L, -2, 1);
    lua_pushnumber(L, debug.currentline);
    lua_rawseti(L, -2, 2);
    lua_pushstring(L, message);
    lua_rawseti(L, -2, 3);

    luaL_traceback(L, L, nullptr, 0);
    lua_rawseti(L, -2, 4);
    return 1;

}

static int ZipLoader(lua_State *L) {
    CLua *cLua = GetCLua(L);
    if (cLua->zipLen == 0) {
        lua_pushstring(L, "Un Init!");
        lua_error(L);
        return 1;
    }
    std::string module = std::string(luaL_checkstring(L, 1));

    std::replace(module.begin(), module.end(), '.', '/');
    module.append(".lua");

    struct zip_t *zip = zip_stream_open(cLua->zip, cLua->zipLen, 0, 'r');
    if (!zip) {
        lua_pushstring(L, "The dependency table does not exist!");
        lua_error(L);
        return 1;
    }
    if (zip_entry_open(zip, module.c_str()) != 0) {
        std::string info;
        info.append("The module \"");
        info.append(module);
        info.append("\" does not exist");
        PushException(L, info.c_str());
        lua_error(L);
        return 1;
    }
    const char *codeBody = NULL;
    size_t codeLen = 0;

    zip_entry_read(zip, (void **) &codeBody, &codeLen);
    zip_entry_close(zip);
    zip_stream_close(zip);
    luaL_loadbuffer(L, codeBody, codeLen, module.c_str());

    if (codeBody) {
        delete codeBody;
    }
    return 1;
}

static void AddCustomLoader(lua_State *L) {
    lua_getglobal(L, "package");  // 获取 package 表
    lua_getfield(L, -1, "searchers");  // 获取 loaders 表
    lua_pushcfunction(L, ZipLoader);  // 推送你的 loader 函数到栈上
    lua_rawseti(L, -2, luaL_len(L, -2) + 1);  // 将 loader 添加到 loaders 表的末尾
    lua_pop(L, 1);  // 弹出 package 表，因为我们完成了操作
}

static int ParseException(lua_State *L) {
    return PushException(L, lua_tostring(L, -1));
}

static void ThrowJvmError(JNIEnv *env, ErrInfo &errInfo) {
    JBootstrap boot(env);
    JLocal<jobject> excRef(env, NewScriptException(env,
            errInfo.code.c_str(),
            errInfo.message.c_str(),
            errInfo.traceback.c_str()));
    env->Throw(static_cast<jthrowable>(excRef.get()));
}

static int WriterDump (lua_State *L, const void *b, size_t size, void *B) {
    luaL_addlstring((luaL_Buffer *) B, (const char *)b, size);
    return 0;
}

static class L2JRet{
    lua_State *L;
    JNIEnv *env;
public:
    L2JRet(lua_State *L,JNIEnv *env) : env(env), L(L) {
    }
    jobject newInt(){
        return NewInt(env,(jint) lua_tonumber(L, -1));
    }
    jobject newDouble(){
        return NewDouble(env,(jdouble) lua_tonumber(L, -1));
    }
    jobject newBool(){
        return NewBool(env,(jboolean) lua_toboolean(L, -1));
    }
    jobject newString(){
        return NewString(env,lua_tostring(L,-1));
    }
    jobject newAny(){
        int type = lua_type(L, -1);
        if (type == LUA_TNUMBER) {
            if(lua_isinteger(L,-1)){
                return this->newInt();
            }else{
                return this->newDouble();
            }
        } else if (type == LUA_TBOOLEAN) {
            return this->newBool();
        } else if (type == LUA_TSTRING) {
            return this->newString();
        } else {
            return nullptr;
        }
    }
};

static class L2JArgs {
private:
    int pos = 2;
    int index = 0;
    JNIEnv *env;
    lua_State *L;
    jobjectArray array;
public:
    L2JArgs(lua_State *L,JNIEnv *env, jobjectArray array, int pos = 2) : env(env), L(L), array(array),pos(pos) {
    }
    void setBytes() {
        luaL_checktype(L, pos, LUA_TSTRING);
        size_t len;
        const char *val = lua_tolstring(L, pos, &len);
        JLocal<jobject> data(env, NewBytes(env, (jbyte *) val, len));
        env->SetObjectArrayElement(array, index, data.get());
    }

    void setNumber() {
        luaL_checktype(L, pos, LUA_TNUMBER);
        if(lua_isinteger(L,pos)){
            JLocal<jobject> data(env, NewLong(env,(jlong) lua_tonumber(L, pos)));
            env->SetObjectArrayElement(array, index, data.get());
        }else{
            JLocal<jobject> data(env, NewDouble(env,(jdouble) lua_tonumber(L, pos)));
            env->SetObjectArrayElement(array, index, data.get());
        }
    }

    void setBool() {
        luaL_checktype(L, pos, LUA_TBOOLEAN);
        JLocal<jobject> data(env, NewBool(env,(jboolean) lua_toboolean(L, pos)));
        env->SetObjectArrayElement(array, index, data.get());
    }

    void setString() {
        luaL_checktype(L, pos, LUA_TSTRING);
        JLocal<jobject> data(env, NewString(env, lua_tostring(L, pos)));
        env->SetObjectArrayElement(array, index, data.get());
    }

    void setCallback(){
        luaL_checktype(L, pos, LUA_TFUNCTION);
        JLocal<jobject> data(env, NewCallback(env, GetCLua(L)->context, pos));
        env->SetObjectArrayElement(array, index, data.get());
    }
    void setFunction(){
        int l = lua_gettop(L);
        luaL_checktype(L, pos, LUA_TFUNCTION);
        luaL_Buffer b;
        luaL_buffinit(L,&b);
        lua_pushvalue(L,pos);
        if (lua_dump(L, WriterDump, &b, false) != 0){
            luaL_error(L, "unable to dump given function");
        }
        JLocal<jbyteArray> ref(env,env->NewByteArray(b.n));
        env->SetByteArrayRegion(ref.get(), 0, b.n, (jbyte*)b.b);
        JLocal<jobject> fileNameJ(env, NewString(env,""));
        JLocal<jobject> lineNumberJ(env, NewInt(env,0));
        JLocal<jobject> func(env,NewFunction(env,ref.get(), fileNameJ.get(), lineNumberJ.get()));
        env->SetObjectArrayElement(array, index, func.get());
        lua_settop(L,l);
    }

    void setAny(){
        int type = lua_type(L, pos);
        if (type == LUA_TNUMBER) {
            this->setNumber();
        } else if (type == LUA_TBOOLEAN) {
            this->setBool();
        } else if (type == LUA_TSTRING) {
            this->setString();
        } else if (type == LUA_TFUNCTION) {
            this->setFunction();
        }
    }

    void setVarargs(){
        int cnt = lua_gettop(L) - pos + 1;
        JLocal<jobjectArray> args(env, NewObjectArray(env,cnt));
        L2JArgs myL2j(L, env, args.get(), pos);
        for (int i = 0; i < cnt; i++) {
            myL2j.setAny();
            myL2j.next();
        }
        JLocal<jobject> varargs(env, NewVarargs(env,args.get()));
        env->SetObjectArrayElement(array, index, varargs.get());
    }

    void next() {
        pos++;
        index++;
    }
};
static void LuaToJava(JNIEnv *env, lua_State *L, const char *buf, jobjectArray array) {
    L2JArgs l2J(L,env,array);
    for (;;) {
        if ((*(buf)) == '\0') {
            break;
        }
        if ((*buf) == ENGINE_TYPE_NUMBER) {
            l2J.setNumber();
        } else if ((*buf) == ENGINE_TYPE_BOOLEAN) {
            l2J.setBool();
        } else if ((*buf) == ENGINE_TYPE_STRING) {
            l2J.setString();
        } else if ((*buf) == ENGINE_TYPE_FUNCTION) {
            l2J.setFunction();
        } else if ((*buf) == ENGINE_TYPE_ANY) {
            l2J.setAny();
        } else if ((*buf) == ENGINE_TYPE_BYTES) {
            l2J.setBytes();
        } else if ((*buf) == ENGINE_TYPE_CALLBACK) {
            l2J.setCallback();
        } else if ((*buf) == ENGINE_TYPE_VARARGS) {
            l2J.setVarargs();
            break;
        }
        l2J.next();
        buf++;
    }
}

static char GetLstChar(const char *argSign){
    if (argSign == nullptr || strlen(argSign) == 0) {
        return '\0';
    }
    size_t len = strlen(argSign);
    char lastChar = argSign[len - 1];
    return lastChar;
}

static void BuildInvokeParam(lua_State *L, JNIEnv *env, const char *argSign, int jLen, int lLen, jobjectArray param) {
    if (lLen < jLen && !((lLen + 1 == jLen) && GetLstChar(argSign) == ENGINE_TYPE_VARARGS)) {
        luaL_error(L, "Requires %d parameters!", jLen);
        return;
    }
    LuaToJava(env, L, argSign, param);
}

static void PushFuncApt(JNIEnv *env, lua_State *L, jobject info);
static void PushObjApt(JNIEnv *env, lua_State *L, jobject _info);

static class J2L{
    lua_State *L;
    JNIEnv *env;
public:
    J2L(lua_State *L, JNIEnv *env) : L(L), env(env) {

    }
    void setInt(jobject data){
        lua_pushinteger(L, ToInt(env,data));
    }
    void setFloat(jobject data){
        lua_pushnumber(L,ToFloat(env,data));
    }
    void setBool(jobject data){
        lua_pushboolean(L, ToBool(env,data));
    }
    void setLong(jobject data){
        lua_pushinteger(L,ToLong(env,data));
    }
    void setDouble(jobject data){
        lua_pushnumber(L, ToDouble(env,data));
    }
    void setVoid(){
        lua_pushnil(L);
    }
    void setString(jobject data){
        JString ret(env,static_cast<jstring>(data));
        lua_pushstring(L, ret.str());
    }
    void setByte(jobject data){
        lua_pushinteger(L, ToByte(env,data));
    }

    void setBytes(jobject data){
        JBytes bytes(env,static_cast<jbyteArray>(data));
        lua_pushlstring(L,bytes.str(),bytes.size());
    }

    void setShort(jobject data){
        lua_pushnumber(L, ToShort(env,data));
    }

    void setFuncApt(jobject data){
        PushFuncApt(env, L, data);
    }
    void setObjApt(jobject data){
        PushObjApt(env, L, data);
    }
    void setAny(jobject data){
        if (IsNull(data)) {
            this->setVoid();
        } else if (IsString(env,data)) {
            this->setString(data);
        } else if (IsBool(env,data)) {
            this->setBool(data);
        } else if (IsInt(env,data)) {
            this->setInt(data);
        } else if (IsLong(env,data)) {
            this->setLong(data);
        } else if (IsFloat(env,data)) {
            this->setFloat(data);
        } else if (IsDouble(env,data)) {
            this->setDouble(data);
        } else if (IsByte(env,data)) {
            this->setByte(data);
        } else if (IsBytes(env,data)) {
            this->setBytes(data);
        } else if (IsShort(env,data)) {
            this->setShort(data);
        } else if (IsFuncApt(env,data)) {
            this->setFuncApt(data);
        } else if (IsObjApt(env,data)) {
            this->setObjApt(data);
        } else {
            lua_pushnil(L);
        }
    }
};

static void CheckJvmError(JNIEnv *env,lua_State *L){
    jstring message = DumpJvmException(env);
    if(message!= nullptr){
        JString msg(env,message);
        luaL_error(L,msg.str());
    }
}

static int RedirectCallerObj(lua_State *L) {
    luaL_checktype(L, 1, LUA_TUSERDATA);
    int lLen = lua_gettop(L) - 1;
    CLua *cLua = GetCLua(L);

    JNIEnv *env = cLua->env;
    jobject caller = ((Apt *) lua_touserdata(L, 1))->object;

    jmethodID invokeMet = (jmethodID) lua_touserdata(L, lua_upvalueindex(1));
    jobject method = (jobject) lua_touserdata(L, lua_upvalueindex(2));
    const char *sign = lua_tostring(L, lua_upvalueindex(3));

    const char *argSign = sign + 1;
    int jLen = strlen(argSign);

    JLocal<jobjectArray> arg(env, NewObjectArray(env,jLen));
    BuildInvokeParam(L, env, argSign, jLen, lLen, arg.get());
    jvalue param[3];
    param[0].l = cLua->context;
    param[1].l = method;
    param[2].l = arg.get();
    jobject _data = env->CallObjectMethodA(caller, invokeMet, param);
    CheckJvmError(env,L);
    JLocal<jobject> data(env, _data);
    if (data.get() != nullptr && IsObjApt(env, data.get())) {
        JLocal<jobjectArray> info(env, UnpackJniBaseApt(env, data.get()));
        JLocal<jobject> apt2(env, JArray(env, info.get()).get(0));
        if (env->IsSameObject(caller, apt2.get())) {
            lua_pushvalue(L, 1);
            return 1;
        }
    }
    J2L j2L(L,env);
    j2L.setAny(data.get());
    return 1;
}

//转发Gc函数至Java层
static int RedirectGc(lua_State *L) {
    luaL_checktype(L, 1, LUA_TUSERDATA);
    CLua *cLua = GetCLua(L);
    JNIEnv *env = cLua->env;
    Apt *adapter = ((Apt *) lua_touserdata(L, 1));
    jobject object = adapter->object;
    jobject *methods = adapter->methods;
    int methodLen = adapter->methodLen;
    jmethodID jme = (jmethodID) lua_touserdata(L, lua_upvalueindex(1));
    env->CallIntMethod(object, jme, cLua->context);
    env->DeleteGlobalRef(object);
    for (int i = 0; i < methodLen; i++) {
        env->DeleteGlobalRef(methods[i]);
    }
    delete methods;
    return 0;
}

static jmethodID GetJMethodID(JNIEnv *env, jclass hClz, jstring name, jstring sign) {
    JString methodName(env, name);
    JString methodSign(env, sign);
    return env->GetMethodID(hClz, methodName.str(), methodSign.str());
}

static void PushObjApt(JNIEnv *env, lua_State *L,
                       jobject object,
                       jstring gcName, jstring gcSign,
                       jstring invokeName, jstring invokeSign,
                       jobjectArray methods,
                       jobjectArray fields) {
    JArray callListRef(env, methods);
    Apt *adapter = (Apt *) lua_newuserdata(L, sizeof(Apt));
    adapter->object = env->NewGlobalRef(object);
    jclass hClz = env->GetObjectClass(adapter->object);
    jmethodID gcMet = GetJMethodID(env, hClz, gcName, gcSign);
    jmethodID invokeMet = GetJMethodID(env, hClz, invokeName, invokeSign);
    jsize callSize = callListRef.size();
    adapter->methods = new jobject[callSize];
    adapter->methodLen = callSize;
    lua_createtable(L, 0, callSize + 1);
    for (int i = 0; i < callSize; i++) {
        JArray callRef(env, static_cast<jobjectArray>(callListRef.get(i)));
        jobject method = env->NewGlobalRef(callRef.get(0));
        adapter->methods[i] = method;
        JString callName(env, static_cast<jstring>(callRef.get(1)));
        JString callSign(env, static_cast<jstring>(callRef.get(2)));

        lua_pushlightuserdata(L, invokeMet);
        lua_pushlightuserdata(L, method);
        lua_pushstring(L, callSign.str());
        lua_pushcclosure(L, RedirectCallerObj, 3);
        lua_setfield(L, -2, callName.str());
    }

    if(fields!= nullptr){
        JArray fieldsRef(env, fields);
        jsize fieldSize = fieldsRef.size();
        for(int i=0;i<fieldSize;i++){
            JLocal<jobjectArray> item(env, static_cast<jobjectArray>(fieldsRef.get(i)));
            JArray fieldRef(env, item.get());
            JLocal<jobject> name(env, fieldRef.get(0));
            JLocal<jobject> value(env, fieldRef.get(1));
            JString javName(env,static_cast<jstring>(name.get()));
            J2L j2L(L,env);
            j2L.setAny(value.get());
            lua_setfield(L, -2, javName.str());
        }
    }

    /*设置GC函数*/
    lua_pushlightuserdata(L, gcMet);
    lua_pushcclosure(L, RedirectGc, 1);
    lua_setfield(L, -2, "__gc");

    /*设置元表为自己*/
    lua_pushvalue(L, -1);
    lua_setfield(L, -2, "__index");
    lua_setmetatable(L, -2);
}

static void PushObjApt(JNIEnv *env, lua_State *L, jobject val) {
    JLocal<jobjectArray> _info(env,UnpackJniBaseApt(env,val));
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
    PushObjApt(env, L,
               apt.get(),
               gcName.get(), gcSign.get(),
               invokeName.get(), invokeSign.get(),
               methods.get(),fields.get());
}

static int RedirectExecute(lua_State *L) {
    int i = lua_gettop(L);
    lua_pushvalue(L, lua_upvalueindex(1));
    lua_getfield(L, -1, __EXECUTE);
    lua_pushvalue(L, i + 1);//将table复制到栈顶,作为参数
    lua_remove(L, i + 1);//移除掉旧table
    for (int n = 0; n < i; n++) {
        //将参数复制到栈顶做准备,从栈低1->n开始复制
        lua_pushvalue(L, n + 1);
    }
    lua_call(L, i + 1, LUA_MULTRET);
    int l2 = lua_gettop(L);
    return l2 - i;
}

static void PushFuncApt(JNIEnv *env, lua_State *L, jobject info) {
    PushObjApt(env, L, info);
    lua_pushcclosure(L, RedirectExecute, 1);
}


CLua::CLua(JNIEnv *env, jobject context,jbyteArray _zip) : env(env) {
    JBytes zip = JBytes(env, _zip);
    lua_State *L = luaL_newstate();
    (*((CLua **) lua_getextraspace(L))) = this;
    this->zipLen = zip.size();
    this->zip = static_cast<const char *>(memcpy(new char[zip.size()], zip.str(), zip.size()));
    this->context = env->NewGlobalRef(context);
    this->bitmap = (Bitmap*)malloc(sizeof(Bitmap));
    this->L = L;
    int it = lua_gettop(L);
    luaL_openlibs(L);
    lua_sethook(L, &LuaInterruptHandler, LUA_MASKLINE, 0);
    AddCustomLoader(L);
    CreateCLib(L);
    CreateImageLib(L);
    CreatePrintLib(L);
    lua_settop(L, it);
}

CLua::~CLua() {
    lua_close(L);
    free(this->bitmap);
    this->env->DeleteGlobalRef(this->context);
    if (this->zipLen > 0) {
        delete this->zip;
    }
}

void CLua::interrupt() {
    this->closed = true;
}

void CLua::setInteger(jstring key, jlong val) {
    lua_pushinteger(L, val);
    JString jvKey = JString(this->env, key);
    SetCLib(L, jvKey.str());
}

void CLua::setNumber(jstring key, jdouble val) {
    lua_pushnumber(L, val);
    JString jvKey = JString(this->env, key);
    SetCLib(L, jvKey.str());
}

void CLua::setString(jstring key, jstring val) {
    JString jvKey = JString(this->env, key);
    JString jvVal = JString(this->env, val);
    lua_pushstring(L, jvVal.str());
    SetCLib(L, jvKey.str());
}

void CLua::setBytes(jstring key, jbyteArray val) {
    JBytes jvVal = JBytes(this->env, val);
    lua_pushlstring(L, jvVal.str(), jvVal.size());
    JString jvKey = JString(this->env, key);
    SetCLib(L, jvKey.str());
}

void CLua::setBool(jstring key, jboolean val) {
    lua_pushboolean(L, val);
    JString jvKey = JString(this->env, key);
    SetCLib(L, jvKey.str());
}

jobject CLua::execute(jstring _module, jstring _func) {
    JString module(env, _module);
    JString func(env, _func);
    int n = lua_gettop(L);
    lua_pushcfunction(L, ParseException); //先将错误处理函数入栈
    int error_pos = lua_gettop(L);
    std::string command;
    command.append("return require(\"");
    command.append(module.str());
    command.append("\");");
    int code = luaL_loadbufferx(L, command.c_str(), command.size(), "<cmdline>", "t");
    ErrInfo errInfo;
    if (CheckLuaException(L, code, errInfo)) {
        lua_settop(L, n);
        ThrowJvmError(env, errInfo);
        return nullptr;
    }
    code = lua_pcall(L, 0, LUA_MULTRET, 0);
    if (CheckLuaException(L, code, errInfo)) {
        lua_settop(L, n);
        ThrowJvmError(env, errInfo);
        return nullptr;
    }
    if (lua_type(L, -1) != LUA_TTABLE) {
        errInfo.message = "The entry table does not exist!";
        lua_settop(L, n);
        ThrowJvmError(env, errInfo);
        return nullptr;
    }
    lua_getfield(L, -1, func.str());
    if (lua_type(L, -1) != LUA_TFUNCTION) {
        std::string info;
        info.append("The entry function \"");
        info.append(func.str());
        info.append("\" does not exist!");
        errInfo.message = info.c_str();
        lua_settop(L, n);
        ThrowJvmError(env, errInfo);
        return nullptr;
    }
    if (CheckLuaException(L, lua_pcall(L, 0, 1, error_pos), errInfo)) {
        lua_settop(L, n);
        ThrowJvmError(env, errInfo);
        return nullptr;
    }
    return L2JRet(L,env).newAny();
}

jobject CLua::executeCmdline(jstring _cmdline, jobjectArray _args) {
    JString cmdline(env,_cmdline);
    lua_pushcfunction(L, ParseException); //先将错误处理函数入栈
    int error_pos = lua_gettop(L);
    int code = luaL_loadbufferx(L, cmdline.str(), cmdline.size(), "<cmdline>", "t");
    ErrInfo errInfo;
    if (CheckLuaException(L, code, errInfo)) {
        ThrowJvmError(env, errInfo);
        return nullptr;
    }
    JArray args(env,_args);
    for (int i = 0; i < args.size(); i++) {
        JLocal<jobject> ref(env,args.get(i));
        J2L j2L(L,env);
        j2L.setAny(ref.get());
    }
    code = lua_pcall(L,args.size(),1,error_pos);
    if (CheckLuaException(L, code, errInfo)) {
        ThrowJvmError(env, errInfo);
        return nullptr;
    }
    return L2JRet(L,env).newAny();
}

void CLua::setObjApt(jstring key, jobject val) {
    PushObjApt(env, L, val);
    JString jvKey = JString(this->env, key);
    SetCLib(L, jvKey.str());
}

void CLua::setFuncApt(jstring key, jobject val) {
    PushFuncApt(env, L, val);
    JString jvKey = JString(this->env, key);
    SetCLib(L, jvKey.str());
}

jobject CLua::executeFunction(jbyteArray _code, jobjectArray _args) {
    int n = lua_gettop(L);
    lua_pushcfunction(L, ParseException); //先将错误处理函数入栈
    int error_pos = lua_gettop(L);
    JBytes code = JBytes(env, _code);
    JArray args(env, _args);
    int status = luaL_loadbufferx(L, code.str(), code.size(), "<function>", "bt");
    ErrInfo errInfo;
    if (CheckLuaException(L, status, errInfo)) {
        lua_settop(L, n);
        ThrowJvmError(env, errInfo);
        return nullptr;
    }
    J2L j2L(L,env);
    int size = args.size();
    for (int i = 0; i < size; i++) {
        JLocal<jobject> item(env, args.get(i));
        j2L.setAny(item.get());
    }
    status = lua_pcall(L,size,1,error_pos);
    if (CheckLuaException(L, status, errInfo)) {
        lua_settop(L, n);
        ThrowJvmError(env, errInfo);
        return nullptr;
    }
    return L2JRet(L,env).newAny();
}

jobject CLua::callback(jlong hold, jobjectArray _args) {
    int n = lua_gettop(L);
    lua_pushcfunction(L, ParseException); //先将错误处理函数入栈
    int error_pos = lua_gettop(L);
    lua_pushvalue(L,hold);
    JArray args(env, _args);
    J2L j2L(L,env);
    int size = args.size();
    for (int i = 0; i < size; i++) {
        JLocal<jobject> item(env, args.get(i));
        j2L.setAny(item.get());
    }
    int status = lua_pcall(L,size,1,error_pos);
    ErrInfo errInfo;
    if (CheckLuaException(L, status, errInfo)) {
        lua_settop(L, n);
        ThrowJvmError(env, errInfo);
        return nullptr;
    }
    return L2JRet(L,env).newAny();
}


void CLua::setBitmap(jint width, jint height, jint row_stride, jint pixel_stride, jobject buff) {
    this->bitmap->width = width;
    this->bitmap->height = height;
    this->bitmap->rowShift = row_stride;
    this->bitmap->pixelStride = pixel_stride;
    this->bitmap->origin = (unsigned char *) env->GetDirectBufferAddress(buff);
}



CLua *openCLua(JNIEnv *env, jobject context,jbyteArray zip) {
    return new CLua(env, context,zip);
}

CLua *toCLua(jlong ptr) {
    return ((CLua *) ptr);
}

void closeCLua(CLua *cjs) {
    delete cjs;
}