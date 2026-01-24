#include <jni.h>
#include <string>

#include "core/bootstrap.h"
#include "core/jvm.h"
#include "cjs/cjs.h"


static jlong newJsRuntime(JNIEnv *env, jclass clazz, jobject context, jbyteArray zip) {
    return reinterpret_cast<jlong>(openCjs(env,context,zip));
}

static void closeJsRuntime(JNIEnv *env, jclass clazz, jlong native_js) {
    closeCjs(toCjs(native_js));
}

static void interrupt(JNIEnv *env, jclass clazz, jlong native_js) {
    toCjs(native_js)->interrupt();
}

static jobject execute(JNIEnv *env, jclass clazz, jlong native_lua, jstring module, jstring func) {
    return toCjs(native_lua)->execute(module, func);
}

static jobject executeCmdline(JNIEnv *env, jclass clazz, jlong native_lua, jstring script,jobjectArray args) {
    return toCjs(native_lua)->executeCmdline(script, args);
}

static jobject  executeFunction(JNIEnv *env, jclass clazz, jlong native_lua,jbyteArray code, jstring file_name, jint line_number, jobjectArray args) {
    return toCjs(native_lua)->executeFunction(code,file_name,line_number,args);
}

static void setLong(JNIEnv *env, jclass clazz, jlong native_js, jstring key, jlong value) {
    toCjs(native_js)->setInteger(key, value);
}

static void setDouble(JNIEnv *env, jclass clazz, jlong native_js, jstring key, jdouble value) {
    toCjs(native_js)->setNumber(key, value);
}

static void setString(JNIEnv *env, jclass clazz, jlong native_js, jstring key, jstring value) {
    toCjs(native_js)->setString(key, value);
}

static void setBytes(JNIEnv *env, jclass clazz, jlong native_js, jstring key, jbyteArray value) {
    toCjs(native_js)->setBytes(key, value);
}

static void setBool(JNIEnv *env, jclass clazz, jlong native_js, jstring key, jboolean value) {
    toCjs(native_js)->setBool(key, value);
}

static void setFuncApt(JNIEnv *env, jclass clazz, jlong native_js, jstring key, jobjectArray value) {
    toCjs(native_js)->setFuncApt(key, value);
}

static void setObjApt(JNIEnv *env, jclass clazz, jlong native_js, jstring key, jobjectArray value) {
    toCjs(native_js)->setObjApt(key, value);
}

static void setBitmap(JNIEnv *env, jclass clazz, jlong native_js, jint width, jint height, jint row_stride, jint pixel_stride, jobject buffer) {
    toCjs(native_js)->setBitmap(width, height, row_stride, pixel_stride, buffer);
}


static jobject callback(JNIEnv *env, jclass clazz, jlong native_js, jlong callback_hold, jobjectArray args) {
    return toCjs(native_js)->callback(callback_hold, args);
}

extern "C" JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = nullptr;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK)
        return -1;
    InitJavaEnv(env);
    InitBootstrapEnv(env);
    JBootstrap boot(env);
    boot.regisNative(100, (void *) (newJsRuntime));
    boot.regisNative(101, (void *) (closeJsRuntime));
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


