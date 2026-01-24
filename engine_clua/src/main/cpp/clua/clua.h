//
// Created by 86152 on 2025/12/14.
//

#ifndef CROBOT_CLUA_H
#define CROBOT_CLUA_H

#include <jni.h>
#include "../lua/lua.hpp"

#define C_ROBOT_LIB "C"
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
#define __EXECUTE "__execute"
#include "../core/image.h"
class CLua {
public:
    lua_State *L;
    JNIEnv *env;
    jobject context;
    Bitmap* bitmap;
    const char *zip;
    size_t zipLen;
    bool closed = false;

    CLua(JNIEnv *env, jobject context,jbyteArray zip);

    ~CLua();

    void interrupt();

    void setInteger(jstring key, jlong val);

    void setNumber(jstring key, jdouble val);

    void setString(jstring key, jstring val);

    void setBytes(jstring key, jbyteArray val);

    void setBool(jstring key, jboolean val);

    jobject execute(jstring module, jstring func);

    jobject executeCmdline(jstring cmdline, jobjectArray args);

    void setObjApt(jstring key, jobject val);

    void setFuncApt(jstring key, jobject val);

    jobject executeFunction(jbyteArray code, jobjectArray args);

    void setBitmap(jint width, jint height, jint row_stride, jint pixel_stride, jobject buffer);

    jobject callback(jlong i, jobjectArray pArray);
};


CLua *openCLua(JNIEnv *env, jobject context,jbyteArray zip);

CLua *toCLua(jlong ptr);

void closeCLua(CLua *cjs);

#endif //CROBOT_CLUA_H
