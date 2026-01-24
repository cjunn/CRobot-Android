#include <jni.h>
#include <string>
#include <gpu.h>
#include <vector>
#include "jvm.h"
#include "yolov8.h"
#include "opencv2/core.hpp"
#include "opencv2/imgproc.hpp"

static YOLOv8 *getYoloV8(int type);

static jlong
newYoloV8(JNIEnv *env, jclass clazz, jstring _param, jstring _bin, jint type, jint target_size,
          jboolean use_gpu) {
    YOLOv8 *yolov8 = getYoloV8(type);
    JString param(env, _param);
    JString bin(env, _bin);
    yolov8->load(param.str(), bin.str(), use_gpu);
    yolov8->set_det_target_size(target_size);
    return reinterpret_cast<jlong>(yolov8);
}


static void bitmapToMat(JNIEnv *env, jobject bitmap, cv::Mat &dst) {
    AndroidBitmapInfo info;
    void *pixels = 0;
    CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
    CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
              info.format == ANDROID_BITMAP_FORMAT_RGB_565);
    CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
    CV_Assert(pixels);
    dst.create(info.height, info.width, CV_8UC4);
    if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
        cv::Mat tmp(info.height, info.width, CV_8UC4, pixels);
        tmp.copyTo(dst);
    } else {
        cv::Mat tmp(info.height, info.width, CV_8UC2, pixels);
        cvtColor(tmp, dst, cv::COLOR_BGR5652RGBA);
    }
    AndroidBitmap_unlockPixels(env, bitmap);
    return;
}

static jobject toList(JNIEnv *env, std::vector<Object> &objects) {
    jobject list = NewList(env);
    JList ret(env, list);
    for (auto it = objects.begin(); it != objects.end(); ++it) {
        Object object = *it;
        JLocal<jobject> _map(env, NewMap(env));
        JMap map(env, _map.get());

        JLocal<jobject> label(env,NewInt(env,object.label));
        JLocal<jobject> prob(env,NewFloat(env,object.prob));
        JLocal<jobject> gindex(env,NewInt(env,object.gindex));
        map.putK("label",label.get());
        map.putK("prob",prob.get());
        map.putK("gindex",gindex.get());

        cv::Rect_<float> rect = object.rect;
        JLocal<jobject> x1(env,NewFloat(env,rect.x));
        JLocal<jobject> y1(env,NewFloat(env,rect.y));
        JLocal<jobject> x2(env,NewFloat(env,rect.x + rect.width));
        JLocal<jobject> y2(env,NewFloat(env,rect.y + rect.height));
        map.putK("x1",x1.get());
        map.putK("y1",y1.get());
        map.putK("x2",x2.get());
        map.putK("y2",y2.get());


        cv::RotatedRect rrect = object.rrect;
        JLocal<jobject> angleX(env,NewFloat(env,rrect.center.x));
        JLocal<jobject> angleY(env,NewFloat(env,rrect.center.y));
        JLocal<jobject> angleW(env,NewFloat(env,rrect.size.width));
        JLocal<jobject> angleH(env,NewFloat(env,rrect.size.height));
        JLocal<jobject> angle(env,NewFloat(env,rrect.angle));
        map.putK("angleX",angleX.get());
        map.putK("angleY",angleY.get());
        map.putK("angleW",angleW.get());
        map.putK("angleH",angleH.get());
        map.putK("angle",angle.get());

        //姿态
        std::vector<KeyPoint> keypoints = object.keypoints;
        JLocal<jobject> _keyList(env,NewList(env));
        JList keyList(env, _keyList.get());
        for (auto it2 = keypoints.begin(); it2 != keypoints.end(); ++it2) {
            KeyPoint keyPoint = *it2;
            JLocal<jobject> _item(env, NewMap(env));
            JMap item(env, _item.get());
            cv::Point2f p = keyPoint.p;

            JLocal<jobject> x(env,NewFloat(env,p.x));
            JLocal<jobject> y(env,NewFloat(env,p.y));
            JLocal<jobject> prob(env,NewFloat(env,keyPoint.prob));

            item.putK("x",x.get());
            item.putK("y",y.get());
            item.putK("prob",prob.get());

            keyList.add(_item.get());
        }
        map.putK("keypoints",_keyList.get());

        //seg数据
        std::vector<cv::Point> segs = object.segs;
        JLocal _segArray(env,NewIntArray(env,segs.size()*2));
        JIntArray segArray(env,_segArray.get());
        int index = 0;
        for (auto it2 = segs.begin(); it2 != segs.end(); ++it2) {
            cv::Point seg = *it2;
            segArray.set(index,seg.x);
            index++;
            segArray.set(index,seg.y);
            index++;
        }
        map.putK("segs",_segArray.get());

        ret.add(_map.get());
    }
    return list;
}

static jobject detect(JNIEnv *env, jclass clazz, jlong ptr, jobject input) {
    YOLOv8 *yolov8 = ((YOLOv8 *) ptr);
    cv::Mat imgRGBA, imgRGB, imgOut;
    bitmapToMat(env, input, imgRGBA);
    cv::cvtColor(imgRGBA, imgRGB, cv::COLOR_RGBA2RGB);
    std::vector<Object> objects;
    yolov8->detect(imgRGB, objects);
    return toList(env, objects);
}


static void close(JNIEnv *env, jclass clazz, jlong ptr) {
    YOLOv8 *yolov8 = ((YOLOv8 *) ptr);
    delete yolov8;
}

extern "C" JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = nullptr;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK)
        return -1;
    InitJavaEnv(env);
    JBootstrap boot(env);
    boot.regisNative(300, (void *) (newYoloV8));
    boot.regisNative(301, (void *) (detect));
    boot.regisNative(302, (void *) (close));
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


static YOLOv8 *getYoloV8(int type) {
    if (type == 0) {
        return new YOLOv8_det_coco;
    }
    if (type == 1) {
        return new YOLOv8_det_oiv7;
    }
    if (type == 2) {
        return new YOLOv8_seg;
    }
    if (type == 3) {
        return new YOLOv8_pose;
    }
    if (type == 4) {
        return new YOLOv8_cls;
    }
    if (type == 5) {
        return new YOLOv8_obb;
    }
    return new YOLOv8_det_coco;
}
