#include <jni.h>
#include <string>
#include "./clua/clua.h"
#include "./core/jvm.h"
#include "./core/bootstrap.h"

static jlong newLuaState(JNIEnv *env, jclass clazz, jobject context, jbyteArray zip) {
    return reinterpret_cast<jlong>(openCLua(env, context, zip));
}

static void closeLuaState(JNIEnv *env, jclass clazz, jlong native_lua) {
    closeCLua(toCLua(native_lua));
}

static void interrupt(JNIEnv *env, jclass clazz, jlong native_lua) {
    toCLua(native_lua)->interrupt();
}

static jobject execute(JNIEnv *env, jclass clazz, jlong native_lua, jstring module, jstring func) {
    return toCLua(native_lua)->execute(module, func);
}

static jobject executeCmdline(JNIEnv *env, jclass clazz, jlong native_lua, jstring script,jobjectArray args) {
    return toCLua(native_lua)->executeCmdline(script, args);
}

static void setLong(JNIEnv *env, jclass clazz, jlong native_lua, jstring key, jlong value) {
    toCLua(native_lua)->setInteger(key, value);
}

static void setDouble(JNIEnv *env, jclass clazz, jlong native_lua, jstring key, jdouble value) {
    toCLua(native_lua)->setNumber(key, value);
}

static void setString(JNIEnv *env, jclass clazz, jlong native_lua, jstring key, jstring _value) {
    toCLua(native_lua)->setString(key, _value);
}

static void setBytes(JNIEnv *env, jclass clazz, jlong native_lua, jstring key, jbyteArray _value) {
    toCLua(native_lua)->setBytes(key, _value);
}

static void setBool(JNIEnv *env, jclass clazz, jlong native_lua, jstring key, jboolean value) {
    toCLua(native_lua)->setBool(key, value);
}

static void setFuncApt(JNIEnv *env, jclass clazz, jlong native_lua, jstring key, jobjectArray value) {
    toCLua(native_lua)->setFuncApt(key, value);
}

static void setObjApt(JNIEnv *env, jclass clazz, jlong native_lua, jstring key, jobjectArray value) {
    toCLua(native_lua)->setObjApt(key, value);
}

static void setBitmap(JNIEnv *env, jclass clazz, jlong native_lua, jint width, jint height, jint row_stride, jint pixel_stride, jobject buffer) {
    toCLua(native_lua)->setBitmap(width, height, row_stride, pixel_stride, buffer);
}

static jobject executeFunction(JNIEnv *env, jclass clazz, jlong native_lua,jbyteArray code, jobjectArray args) {
    return toCLua(native_lua)->executeFunction(code, args);
}

static jobject callback(JNIEnv *env, jclass clazz, jlong native_lua, jlong hold, jobjectArray args) {
    return toCLua(native_lua)->callback(hold, args);
}


extern "C" JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = nullptr;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK)
        return -1;
    InitJavaEnv(env);
    InitBootstrapEnv(env);
    JBootstrap boot(env);
    boot.regisNative(100, (void *) (newLuaState));
    boot.regisNative(101, (void *) (closeLuaState));
    boot.regisNative(102, (void *) (interrupt));
    boot.regisNative(103, (void *) (execute));
    boot.regisNative(104, (void *) (executeCmdline));
    boot.regisNative(105, (void *) (setLong));
    boot.regisNative(106, (void *) (setDouble));
    boot.regisNative(107, (void *) (setString));
    boot.regisNative(108, (void *) (setBytes));
    boot.regisNative(109, (void *) (setBool));
    boot.regisNative(110, (void *) (setFuncApt));
    boot.regisNative(111, (void *) (setObjApt));
    boot.regisNative(112, (void *) (setBitmap));
    boot.regisNative(113, (void *) (executeFunction));
    boot.regisNative(114, (void *) (callback));
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv *env = nullptr;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK)
        return;
    UnloadJavaEnv(env);
    UnloadBootstrapEnv(env);
}

