//
// Created by cjunh on 2026/1/14.
//

#include "jvm.h"

static jclass listClz;
static jclass mapClz;
static jclass byteArrayClz;
static jclass arrayClz;
static jclass objectClz;
static jclass stringClz;
static jclass boolClz;
static jclass intClz;
static jclass longClz;
static jclass floatClz;
static jclass doubleClz;
static jclass byteClz;
static jclass shortClz;
static jclass charClz;
static jclass bootClz;

static jmethodID boolMethod;
static jmethodID intMethod;
static jmethodID longMethod;
static jmethodID floatMethod;
static jmethodID doubleMethod;
static jmethodID byteMethod;
static jmethodID shortMethod;
static jmethodID charMethod;
static jmethodID hashMapInit;
static jmethodID arrayListInit;
static jmethodID mapPutMethod;
static jmethodID mapGetMethod;
static jmethodID listGetMethod;
static jmethodID listAddMethod;
static jmethodID listSizeMethod;
static jmethodID newInstanceMethod;
static jmethodID throwableGetMessageMethod;
static jmethodID throwableToStringMethod;

static jfieldID boolField;
static jfieldID intField;
static jfieldID longField;
static jfieldID floatField;
static jfieldID doubleField;
static jfieldID byteField;
static jfieldID shortField;
static jfieldID charField;
static jfieldID bootField;

void InitJavaEnv(JNIEnv *env) {
    bootClz = static_cast<jclass>(env->NewGlobalRef(env->FindClass("android/boostrap/CBootstrap")));
    listClz = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/util/ArrayList")));
    mapClz = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/util/HashMap")));
    byteArrayClz = static_cast<jclass>(env->NewGlobalRef(env->FindClass("[B")));
    arrayClz = static_cast<jclass>(env->NewGlobalRef(env->FindClass("[Ljava/lang/Object;")));
    objectClz = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/lang/Object")));
    stringClz = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/lang/String")));
    boolClz = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/lang/Boolean")));
    intClz = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/lang/Integer")));
    longClz = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/lang/Long")));
    floatClz = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/lang/Float")));
    doubleClz = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/lang/Double")));
    byteClz = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/lang/Byte")));
    shortClz = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/lang/Short")));
    charClz = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/lang/Character")));

    boolMethod = env->GetStaticMethodID(boolClz, "valueOf", "(Z)Ljava/lang/Boolean;");
    intMethod = env->GetStaticMethodID(intClz, "valueOf", "(I)Ljava/lang/Integer;");
    longMethod = env->GetStaticMethodID(longClz, "valueOf", "(J)Ljava/lang/Long;");
    floatMethod = env->GetStaticMethodID(floatClz, "valueOf", "(F)Ljava/lang/Float;");
    doubleMethod = env->GetStaticMethodID(doubleClz, "valueOf", "(D)Ljava/lang/Double;");
    byteMethod = env->GetStaticMethodID(byteClz, "valueOf", "(B)Ljava/lang/Byte;");
    shortMethod = env->GetStaticMethodID(shortClz, "valueOf", "(S)Ljava/lang/Short;");
    charMethod = env->GetStaticMethodID(charClz, "valueOf", "(C)Ljava/lang/Character;");
    hashMapInit = env->GetMethodID(mapClz, "<init>", "()V");
    arrayListInit = env->GetMethodID(listClz, "<init>", "()V");
    mapPutMethod = env->GetMethodID(mapClz, "put","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
    mapGetMethod = env->GetMethodID(mapClz, "get","(Ljava/lang/Object;)Ljava/lang/Object;");
    listGetMethod = env->GetMethodID(listClz, "get", "(I)Ljava/lang/Object;");
    listAddMethod = env->GetMethodID(listClz, "add", "(Ljava/lang/Object;)Z");
    listSizeMethod = env->GetMethodID(listClz, "size", "()I");
    JLocal<jclass> constructorClz(env,static_cast<jclass>(env->FindClass("java/lang/reflect/Constructor")));
    newInstanceMethod = env->GetMethodID(constructorClz.get(), "newInstance","([Ljava/lang/Object;)Ljava/lang/Object;");
    JLocal<jclass> throwableClz(env,static_cast<jclass>(env->FindClass("java/lang/Throwable")));
    throwableGetMessageMethod = env->GetMethodID(throwableClz.get(), "getMessage","()Ljava/lang/String;");
    throwableToStringMethod = env->GetMethodID(throwableClz.get(), "toString","()Ljava/lang/String;");

    boolField = env->GetFieldID(boolClz, "value", "Z");
    intField = env->GetFieldID(intClz, "value", "I");
    longField = env->GetFieldID(longClz, "value", "J");
    floatField = env->GetFieldID(floatClz, "value", "F");
    doubleField = env->GetFieldID(doubleClz, "value", "D");
    byteField = env->GetFieldID(byteClz, "value", "B");
    shortField = env->GetFieldID(shortClz, "value", "S");
    charField = env->GetFieldID(charClz, "value", "C");
    bootField = env->GetStaticFieldID(bootClz, "LINK", "Ljava/util/Map;");
}

void UnloadJavaEnv(JNIEnv *env) {
    env->DeleteLocalRef(listClz);
    env->DeleteGlobalRef(mapClz);
    env->DeleteGlobalRef(byteArrayClz);
    env->DeleteGlobalRef(arrayClz);
    env->DeleteGlobalRef(objectClz);
    env->DeleteGlobalRef(stringClz);
    env->DeleteGlobalRef(boolClz);
    env->DeleteGlobalRef(intClz);
    env->DeleteGlobalRef(longClz);
    env->DeleteGlobalRef(floatClz);
    env->DeleteGlobalRef(doubleClz);
    env->DeleteGlobalRef(byteClz);
    env->DeleteGlobalRef(shortClz);
    env->DeleteGlobalRef(charClz);
}


bool IsNull(jobject v) {
    return v == nullptr;
}

bool IsBool(JNIEnv *env, jobject v) {
    return env->IsInstanceOf(v, boolClz);
}

bool IsInt(JNIEnv *env, jobject v) {
    return env->IsInstanceOf(v, intClz);
}

bool IsLong(JNIEnv *env, jobject v) {
    return env->IsInstanceOf(v, longClz);
}

bool IsFloat(JNIEnv *env, jobject v) {
    return env->IsInstanceOf(v, floatClz);
}

bool IsDouble(JNIEnv *env, jobject v) {
    return env->IsInstanceOf(v, doubleClz);
}

bool IsByte(JNIEnv *env, jobject v) {
    return env->IsInstanceOf(v, byteClz);
}

bool IsShort(JNIEnv *env, jobject v) {
    return env->IsInstanceOf(v, shortClz);
}

bool IsChar(JNIEnv *env, jobject v) {
    return env->IsInstanceOf(v, charClz);
}

bool IsString(JNIEnv *env, jobject v) {
    return env->IsInstanceOf(v, stringClz);
}

bool IsArray(JNIEnv *env, jobject v) {
    return env->IsInstanceOf(v, arrayClz);
}

bool IsBytes(JNIEnv *env, jobject v) {
    return env->IsInstanceOf(v, byteArrayClz);
}


jobject NewBool(JNIEnv *env, jboolean v) {
    return env->CallStaticObjectMethod(boolClz, boolMethod, v);
}

jobject NewInt(JNIEnv *env, jint v) {
    return env->CallStaticObjectMethod(intClz, intMethod, v);
}

jobject NewLong(JNIEnv *env, jlong v) {
    return env->CallStaticObjectMethod(longClz, longMethod, v);
}

jobject NewFloat(JNIEnv *env, jfloat v) {
    return env->CallStaticObjectMethod(floatClz, floatMethod, v);
}

jobject NewDouble(JNIEnv *env, jdouble v) {
    return env->CallStaticObjectMethod(doubleClz, doubleMethod, v);
}

jobject NewByte(JNIEnv *env, jbyte v) {
    return env->CallStaticObjectMethod(byteClz, byteMethod, v);
}

jobject NewShort(JNIEnv *env, jshort v) {
    return env->CallStaticObjectMethod(shortClz, shortMethod, v);
}

jobject NewChar(JNIEnv *env, jchar v) {
    return env->CallStaticObjectMethod(charClz, charMethod, v);
}

jstring NewString(JNIEnv *env, const char *v) {
    return env->NewStringUTF(v);
}
jobject NewList(JNIEnv *env) {
    return env->NewObject(listClz, arrayListInit);
}
jobject NewMap(JNIEnv *env) {
    return env->NewObject(mapClz, hashMapInit);
}

jbyteArray NewBytes(JNIEnv *env, jbyte *val, size_t len) {
    jbyteArray byte_array = env->NewByteArray(len);
    env->SetByteArrayRegion(byte_array, 0, len, val);
    return byte_array;
}

jobjectArray NewObjectArray(JNIEnv *env, int len) {
    return env->NewObjectArray(len, objectClz, NULL);
}

jobject NewInstance(JNIEnv *env, jobject constructor, jobjectArray params) {
    return env->CallObjectMethod(constructor, newInstanceMethod, params);
}

jboolean ToBool(JNIEnv *env, jobject v) {
    return env->GetBooleanField(v, boolField);
}

jint ToInt(JNIEnv *env, jobject v) {
    return env->GetIntField(v, intField);
}

jlong ToLong(JNIEnv *env, jobject v) {
    return env->GetLongField(v, longField);
}

jfloat ToFloat(JNIEnv *env, jobject v) {
    return env->GetFloatField(v, floatField);
}

jdouble ToDouble(JNIEnv *env, jobject v) {
    return env->GetDoubleField(v, doubleField);
}

jbyte ToByte(JNIEnv *env, jobject v) {
    return env->GetByteField(v, byteField);
}

jshort ToShort(JNIEnv *env, jobject v) {
    return env->GetShortField(v, shortField);
}

jchar ToChar(JNIEnv *env, jobject v) {
    return env->GetCharField(v, charField);
}

jobject ToBootRegMap(JNIEnv *env){
    return env->GetStaticObjectField(bootClz, bootField);
}

JString::JString(JNIEnv *env, jstring string) : env(env), string(string) {
    jboolean isCopy = 0;
    chars = env->GetStringUTFChars(string, &isCopy);
    len = env->GetStringUTFLength(string);
}

JString::~JString() {
    env->ReleaseStringUTFChars(string, chars);
}

const char *JString::str() {
    return chars;
}

jsize JString::size() const {
    return len;
}

JArray::JArray(JNIEnv *env, jobjectArray value) : env(env), value(value) {
    this->len = env->GetArrayLength(value);
}

jobject JArray::get(int index) {
    if (index >= len) {
        return nullptr;
    }
    return env->GetObjectArrayElement(value, index);
}

int JArray::size() {
    return this->len;
}

JBytes::JBytes(JNIEnv *env, jbyteArray jBytes) : env(env), jBytes(jBytes) {
    jboolean isCopy = 0;
    bytes = (char *) env->GetByteArrayElements(jBytes, &isCopy);
    len = env->GetArrayLength(jBytes);
}

JBytes::~JBytes() {
    env->ReleaseByteArrayElements(jBytes, (jbyte *) bytes, 0);
}

const char *JBytes::str() {
    return bytes;
}

jsize JBytes::size() const {
    return len;
}

JList::JList(JNIEnv *env, jobject obj) : env(env), obj(obj) {
}

jint JList::size() const {
    return env->CallIntMethod(obj, listSizeMethod);
}

jobject JList::at(jint index) {
    return env->CallObjectMethod(obj, listGetMethod, index);
}

jboolean JList::add(jobject value) {
    return env->CallBooleanMethod(obj, listAddMethod, value);
}

JMap::JMap(JNIEnv *env, jobject obj) : env(env), obj(obj) {
}

void JMap::put(jobject key, jobject value) {
    jvalue param[2];
    param[0].l = key;
    param[1].l = value;
    env->CallObjectMethodA(obj, mapPutMethod, param);
}

void JMap::putK(const char* key, jobject value) {
    JLocal<jstring> ke(env, NewString(env,key));
    this->put(ke.get(),value);
}

jobject JMap::get(jobject key) {
    jvalue param[1];
    param[0].l = key;
    return env->CallObjectMethodA(obj,mapGetMethod,param);;
}

JBootstrap::JBootstrap(JNIEnv *env) : env(env) {
    map = ToBootRegMap(env);
}

JBootstrap::~JBootstrap() {
    if (map) {
        env->DeleteLocalRef(map);
    }
}

jobject JBootstrap::getBoot(int askType) {
    JMap regMap(env,map);
    JLocal<jobject> key(env,NewInt(env,askType));
    return regMap.get(key.get());
}


static int BOOT_NATIVE_FUNC = 1;

void JBootstrap::regisNative(int index, void *func) {
    JLocal<jobject> _nativeMap(env, getBoot(BOOT_NATIVE_FUNC));
    if (_nativeMap.get() == nullptr) {
        return;
    }
    JMap nativeMap(env, _nativeMap.get());
    JLocal<jobject> key(env, NewInt(env, index));
    JLocal<jobject> value(env, nativeMap.get(key.get()));
    if (value.get() == nullptr) {
        return;
    }
    JArray regInfo(env, static_cast<jobjectArray>(value.get()));
    JLocal<jclass> regClz(env, static_cast<jclass>(regInfo.get(0)));
    JLocal<jstring> regMethodName(env, static_cast<jstring>(regInfo.get(1)));
    JLocal<jstring> regMethodSign(env, static_cast<jstring>(regInfo.get(2)));
    JString regMethodNameS(env, regMethodName.get());
    JString regMethodSignS(env, regMethodSign.get());
    JNINativeMethod registerMethod[] = {
            {regMethodNameS.str(), regMethodSignS.str(), func}
    };
    env->RegisterNatives(regClz.get(), registerMethod, 1);
}


jclass JBootstrap::getClazz(int askType) {
    JLocal<jobject> askVal(env, getBoot(askType));
    if (askVal.get() == nullptr) {
        return nullptr;
    }
    JArray askParams(env, static_cast<jobjectArray>(askVal.get()));
    return static_cast<jclass>(askParams.get(0));
}

jmethodID JBootstrap::getMethod(int askType) {
    JLocal<jobject> askVal(env, getBoot(askType));
    if (askVal.get() == nullptr) {
        return nullptr;
    }
    JArray askParams(env, static_cast<jobjectArray>(askVal.get()));
    JLocal<jclass> clz(env, static_cast<jclass>(askParams.get(0)));
    JLocal<jstring> name(env, static_cast<jstring>(askParams.get(1)));
    JLocal<jstring> sign(env, static_cast<jstring>(askParams.get(2)));
    JString nameS(env, name.get());
    JString signS(env, sign.get());
    return env->GetMethodID(clz.get(), nameS.str(), signS.str());
}


jobject JBootstrap::getConstructor(int askType) {
    JLocal<jobject> askVal(env, getBoot(askType));
    if (askVal.get() == nullptr) {
        return nullptr;
    }
    JArray askParams(env, static_cast<jobjectArray>(askVal.get()));
    return askParams.get(0);
}


jstring DumpJvmException(JNIEnv *env){
    jthrowable ex = env->ExceptionOccurred();
    if (ex == NULL) {
        return nullptr;
    }
    env->ExceptionClear();
    jstring ret = static_cast<jstring>(env->CallObjectMethod(ex, throwableGetMessageMethod));
    if (ret != nullptr) {
        return ret;
    }
    return static_cast<jstring>(env->CallObjectMethod(ex, throwableToStringMethod));
}