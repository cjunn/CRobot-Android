//
// Created by cjunh on 2026/1/14.
//

#include "bootstrap.h"
#include "jvm.h"
static jclass jniFuncAptClz;
static jclass jniObjAptClz;
static jclass objectPtrClz;
static jclass jsonBeanClz;
static jobject varargsCons;
static jobject callbackCons;
static jobject objectPtrCons;
static jobject functionCons;
static jobject scriptExceptionCons;

static jmethodID jniBaseAptObjectsMethod;
static jmethodID jniObjectPtrGetMethod;
static jmethodID jniJsonBeanGetMethod;


static int BOOT_SCRIPT_EXCEPTION_CONS = 2;
static int BOOT_FUNCTION_CONS = 4;
static int BOOT_VARARGS_CONS = 5;
static int BOOT_JNI_OBJ_APT_CLZ = 6;
static int BOOT_JNI_FUNC_APT_CLZ = 7;
static int BOOT_JNI_BASE_APT_OBJECTS_MET = 8;
static int BOOT_JNI_OBJECT_PTR_CONS = 9;
static int BOOT_JNI_OBJECT_PTR_GET_MET = 10;
static int BOOT_JNI_OBJECT_PTR_CLZ = 11;
static int BOOT_CALLBACK_CONS = 12;
static int BOOT_JNI_JSON_BEAN_CLZ = 13;
static int BOOT_JNI_JSON_BEAN_GET_MET = 14;

void InitBootstrapEnv(JNIEnv *env) {
    JBootstrap boot(env);
    jniBaseAptObjectsMethod = boot.getMethod(BOOT_JNI_BASE_APT_OBJECTS_MET);
    jniObjectPtrGetMethod = boot.getMethod(BOOT_JNI_OBJECT_PTR_GET_MET);
    jniJsonBeanGetMethod = boot.getMethod(BOOT_JNI_JSON_BEAN_GET_MET);

    jniFuncAptClz = static_cast<jclass>(env->NewGlobalRef(boot.getClazz(BOOT_JNI_FUNC_APT_CLZ)));
    jniObjAptClz = static_cast<jclass>(env->NewGlobalRef(boot.getClazz(BOOT_JNI_OBJ_APT_CLZ)));
    objectPtrClz = static_cast<jclass>(env->NewGlobalRef(boot.getClazz(BOOT_JNI_OBJECT_PTR_CLZ)));
    jsonBeanClz = static_cast<jclass>(env->NewGlobalRef(boot.getClazz(BOOT_JNI_JSON_BEAN_CLZ)));
    varargsCons = env->NewGlobalRef(boot.getConstructor(BOOT_VARARGS_CONS));
    callbackCons = env->NewGlobalRef(boot.getConstructor(BOOT_CALLBACK_CONS));
    functionCons = env->NewGlobalRef(boot.getConstructor(BOOT_FUNCTION_CONS));
    scriptExceptionCons = env->NewGlobalRef(boot.getConstructor(BOOT_SCRIPT_EXCEPTION_CONS));
    objectPtrCons = env->NewGlobalRef(boot.getConstructor(BOOT_JNI_OBJECT_PTR_CONS));
}

void UnloadBootstrapEnv(JNIEnv *env) {
    env->DeleteGlobalRef(callbackCons);
    env->DeleteGlobalRef(varargsCons);
    env->DeleteGlobalRef(functionCons);
    env->DeleteGlobalRef(scriptExceptionCons);
    env->DeleteGlobalRef(objectPtrCons);

    env->DeleteGlobalRef(jniFuncAptClz);
    env->DeleteGlobalRef(jniObjAptClz);
    env->DeleteGlobalRef(objectPtrClz);
    env->DeleteGlobalRef(jsonBeanClz);
}

bool IsFuncApt(JNIEnv *env, jobject v) {
    return env->IsInstanceOf(v, jniFuncAptClz);
}
bool IsObjApt(JNIEnv *env, jobject v) {
    return env->IsInstanceOf(v, jniObjAptClz);
}
bool IsObjectPtr(JNIEnv *env, jobject v) {
    return env->IsInstanceOf(v, objectPtrClz);
}
bool ISJsonBean(JNIEnv *env, jobject v){
    return env->IsInstanceOf(v, jsonBeanClz);
}

jobject NewCallback(JNIEnv *env,jobject context,jlong _hold){
    JLocal<jobjectArray> params(env, NewObjectArray(env,2));
    JLocal<jobject> hold(env, NewLong(env,_hold));
    env->SetObjectArrayElement(params.get(), 0, context);
    env->SetObjectArrayElement(params.get(), 1, hold.get());
    return NewInstance(env,callbackCons,params.get());
}

jobject NewVarargs(JNIEnv *env,jobjectArray array) {
    JLocal<jobjectArray> params(env, NewObjectArray(env,1));
    env->SetObjectArrayElement(params.get(), 0, array);
    return NewInstance(env,varargsCons,params.get());
}

jobject NewFunction(JNIEnv *env,jbyteArray array,jobject fileName,jobject lineNumber){
    JLocal<jobjectArray> params(env,  NewObjectArray(env,3));
    env->SetObjectArrayElement(params.get(), 0, array);
    env->SetObjectArrayElement(params.get(), 1, fileName);
    env->SetObjectArrayElement(params.get(), 2, lineNumber);
    return NewInstance(env,functionCons,params.get());
}

jobject NewScriptException(JNIEnv *env,const char *code, const char *message, const char *traceback){
    JLocal<jobjectArray> params(env,  NewObjectArray(env,3));
    JLocal<jobject> codeS = JLocal<jobject>(env,NewString(env, code));
    JLocal<jobject> messageS = JLocal<jobject>(env,NewString(env, message));
    JLocal<jobject> traceS = JLocal<jobject>(env,NewString(env, traceback));

    env->SetObjectArrayElement(params.get(), 0, codeS.get());
    env->SetObjectArrayElement(params.get(), 1, messageS.get());
    env->SetObjectArrayElement(params.get(), 2, traceS.get());
    return NewInstance(env,scriptExceptionCons,params.get());
}

jobject NewObjectPtr(JNIEnv *env,void* ptr){
    jlong p = (jlong)ptr;
    JLocal<jobjectArray> params(env,  NewObjectArray(env,1));
    JLocal<jobject> pJ = JLocal<jobject>(env, NewLong(env,p));
    env->SetObjectArrayElement(params.get(), 0, pJ.get());
    return NewInstance(env,objectPtrCons,params.get());
}

void *ToObjectPtr(JNIEnv *env, jobject v) {
    return (void *) env->CallLongMethod(v, jniObjectPtrGetMethod);
}

void JsonBeanToString(JNIEnv *env, jobject v,std::string& body){
    JLocal<jobject> ret(env,env->CallObjectMethod(v, jniJsonBeanGetMethod));
    JString retS(env,reinterpret_cast<jstring>(ret.get()));
    body.append(retS.str());
}


jobjectArray UnpackJniBaseApt(JNIEnv *env,jobject obj){
    return static_cast<jobjectArray>(env->CallObjectMethod(obj, jniBaseAptObjectsMethod));
}
