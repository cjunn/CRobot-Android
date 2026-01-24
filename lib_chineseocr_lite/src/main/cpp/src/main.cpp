#include <jni.h>
#include <string>
#include "OcrLite.h"
#include "jvm.h"

static jlong newOcrLite(JNIEnv *env, jclass clazz,
                                    jstring _angle_param, jstring _angle_bin,
                                    jstring _db_param, jstring _db_bin,
                                    jstring _crnn_param, jstring _crnn_bin,
                                    jstring _keys, jint numOfThread,jboolean useGpu) {
    JString angleParam(env, _angle_param);
    JString angleBin(env, _angle_bin);
    JString dbParam(env, _db_param);
    JString dbBin(env, _db_bin);
    JString crnnParam(env, _crnn_param);
    JString crnnBin(env, _crnn_bin);
    JString keys(env, _keys);
    OcrLite *ocr = new OcrLite();
    ocr->init(angleParam.str(), angleBin.str(),
              dbParam.str(), dbBin.str(),
              crnnParam.str(), crnnBin.str(),
              keys.str(), numOfThread,useGpu);
    return reinterpret_cast<jlong>(ocr);
}

static void close(JNIEnv *env, jclass clazz, jlong ptr) {
    OcrLite *ocr = ((OcrLite *) ptr);
    delete ocr;
}


static jobject detect(JNIEnv *env, jclass clazz, jlong ptr,
                                  jobject input, jint padding, jint max_side_len,
                                  jfloat box_score_thresh, jfloat box_thresh,
                                  jfloat un_clip_ratio, jboolean do_angle,
                                  jboolean most_angle) {
    OcrLite *ocr = ((OcrLite *) ptr);
    return ocr->detect(env, input, padding, max_side_len, box_score_thresh, box_thresh,
                       un_clip_ratio, do_angle, most_angle);
}


extern "C" JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = nullptr;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK)
        return -1;
    InitJavaEnv(env);
    JBootstrap boot(env);
    boot.regisNative(200, (void *) (newOcrLite));
    boot.regisNative(201, (void *) (detect));
    boot.regisNative(202, (void *) (close));
    ncnn::create_gpu_instance();
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv *env = nullptr;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK)
        return;
    UnloadJavaEnv(env);
    ncnn::destroy_gpu_instance();
}

