//
// Created by cjunh on 2026/1/14.
//

#ifndef CROBOT_BOOTSTRAP_H
#define CROBOT_BOOTSTRAP_H
#include <jni.h>

void InitBootstrapEnv(JNIEnv *env);

void UnloadBootstrapEnv(JNIEnv *env);

bool IsFuncApt(JNIEnv *env, jobject v);
bool IsObjApt(JNIEnv *env, jobject v);
bool IsObjectPtr(JNIEnv *env, jobject v);
jobject NewCallback(JNIEnv *env,jobject context,jlong hold);
jobject NewVarargs(JNIEnv *env,jobjectArray array);
jobject NewFunction(JNIEnv *env, jbyteArray array, jobject fileName, jobject lineNumber);
jobject NewScriptException(JNIEnv *env,const char *code, const char *message, const char *traceback);
jobject NewObjectPtr(JNIEnv *env,void* ptr);
void* ToObjectPtr(JNIEnv *env,jobject v);
jobjectArray UnpackJniBaseApt(JNIEnv *env,jobject obj);

#endif //CROBOT_BOOTSTRAP_H
