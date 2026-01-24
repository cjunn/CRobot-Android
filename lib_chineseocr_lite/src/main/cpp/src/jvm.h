//
// Created by cjunh on 2026/1/14.
//

#ifndef CROBOT_JVM_H
#define CROBOT_JVM_H

#include <jni.h>
#include <android/log.h>
#define LOG_TAG "C_AUTO_LUA"
#define CLogD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

void InitJavaEnv(JNIEnv *env);

void UnloadJavaEnv(JNIEnv *env);

bool IsNull(jobject v);

bool IsBool(JNIEnv *env, jobject v);

bool IsInt(JNIEnv *env, jobject v);

bool IsLong(JNIEnv *env, jobject v);

bool IsFloat(JNIEnv *env, jobject v);

bool IsDouble(JNIEnv *env, jobject v);

bool IsByte(JNIEnv *env, jobject v);

bool IsShort(JNIEnv *env, jobject v);

bool IsChar(JNIEnv *env, jobject v);

bool IsString(JNIEnv *env, jobject v);

bool IsArray(JNIEnv *env, jobject v);

bool IsBytes(JNIEnv *env, jobject v);

jobject NewBool(JNIEnv *env, jboolean v);

jobject NewInt(JNIEnv *env, jint v);

jobject NewLong(JNIEnv *env, jlong v);

jobject NewFloat(JNIEnv *env, jfloat v);

jobject NewDouble(JNIEnv *env, jdouble v);

jobject NewByte(JNIEnv *env, jbyte v);

jobject NewShort(JNIEnv *env, jshort v);

jobject NewChar(JNIEnv *env, jchar v);

jstring NewString(JNIEnv *env, const char *v);
jobject NewList(JNIEnv *env);
jobject NewMap(JNIEnv *env);

jbyteArray NewBytes(JNIEnv *env, jbyte *val, size_t len);

jobjectArray NewObjectArray(JNIEnv *env, int len);

jobject NewInstance(JNIEnv *env, jobject constructor, jobjectArray params);

jboolean ToBool(JNIEnv *env, jobject v);

jint ToInt(JNIEnv *env, jobject v);

jlong ToLong(JNIEnv *env, jobject v);

jfloat ToFloat(JNIEnv *env, jobject v);

jdouble ToDouble(JNIEnv *env, jobject v);

jbyte ToByte(JNIEnv *env, jobject v);

jshort ToShort(JNIEnv *env, jobject v);

jchar ToChar(JNIEnv *env, jobject v);

jobject ToBootRegMap(JNIEnv *env, jobject v);

template<class T>
class JLocal {
    JNIEnv *env;
    T v;
public:
    JLocal(JNIEnv *env, T v) : env(env), v(v) {

    }

    ~JLocal() {
        if (v) {
            env->DeleteLocalRef((jobject) v);
        }
    }

    T get() {
        return v;
    }
};

class JString {
    JNIEnv *env;
    jstring string;
    const char *chars;
    int len;
public:
    JString(JNIEnv *env, jstring string);

    ~JString();

    const char *str();

    jsize size() const;
};

class JArray {
    JNIEnv *env;
    jobjectArray value;
    int len;
public:
    JArray(JNIEnv *env, jobjectArray value);

    jobject get(int index);

    int size();
};

class JBytes {
    JNIEnv *env;
    jbyteArray jBytes;
    const char *bytes;
    jsize len;
public:
    JBytes(JNIEnv *env, jbyteArray jBytes);

    ~JBytes();

    const char *str();

    jsize size() const;
};

class JList {
    JNIEnv *env;
    jobject obj;
public:
    JList(JNIEnv *env, jobject obj);

    jint size() const;

    jobject at(jint index);
    jboolean add(jobject value);
};

class JMap {
    JNIEnv *env;
    jobject obj;
public:
    JMap(JNIEnv *env, jobject obj);

    void put(jobject key, jobject value);
    void putK(const char* key,jobject value);
    jobject get(jobject key);
};


class JBootstrap {
    JNIEnv *env;
    jobject map;
public:
    JBootstrap(JNIEnv *env);
    ~JBootstrap();
    jobject getBoot(int askType);
    void regisNative(int index, void *func);
    jclass getClazz(int askType);
    jmethodID getMethod(int askType);
    jobject getConstructor(int askType);
};

jstring DumpJvmException(JNIEnv *env);



#endif //CROBOT_JVM_H
