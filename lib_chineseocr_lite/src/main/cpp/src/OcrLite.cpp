//
// Created by 86152 on 2026/1/13.
//

#include <opencv2/imgproc.hpp>
#include "OcrLite.h"
#include "opencv2/core.hpp"
#include "opencv2/imgproc.hpp"
#include "jvm.h"

OcrLite::OcrLite() {}

static int getThickness(cv::Mat &boxImg) {
    int minSize = boxImg.cols > boxImg.rows ? boxImg.rows : boxImg.cols;
    int thickness = minSize / 1000 + 2;
    return thickness;
}

static double getCurrentTime() {
    return (static_cast<double>(cv::getTickCount())) / cv::getTickFrequency() * 1000;//单位毫秒
}

static void drawTextBox(cv::Mat &boxImg, cv::RotatedRect &rect, int thickness) {
    cv::Point2f vertices[4];
    rect.points(vertices);
    for (int i = 0; i < 4; i++)
        cv::line(boxImg, vertices[i], vertices[(i + 1) % 4], cv::Scalar(0, 0, 255), thickness);
}

void drawTextBox(cv::Mat &boxImg, const std::vector<cv::Point> &box, int thickness) {
    auto color = cv::Scalar(255, 0, 0);// R(255) G(0) B(0)
    cv::line(boxImg, box[0], box[1], color, thickness);
    cv::line(boxImg, box[1], box[2], color, thickness);
    cv::line(boxImg, box[2], box[3], color, thickness);
    cv::line(boxImg, box[3], box[0], color, thickness);
}

static void drawTextBoxes(cv::Mat &boxImg, std::vector<TextBox> &textBoxes, int thickness) {
    for (int i = 0; i < textBoxes.size(); ++i) {
        drawTextBox(boxImg, textBoxes[i].boxPoint, thickness);
    }
}

void OcrLite::init(const char *angle_param, const char *angle_bin,
                   const char *db_param, const char *db_bin,
                   const char *crnn_param, const char *crnn_bin,
                   const char *keys, int numOfThread,bool useGpu) {

    if (useGpu && ncnn::get_gpu_count() == 0) {
        useGpu = false;
    }
    this->useGpu = useGpu;
    angleWork.initModel(angle_param, angle_bin);
    angleWork.setNumThread(numOfThread);
    angleWork.setUseGpu(useGpu);
    dbWork.initModel(db_param, db_bin);
    dbWork.setNumThread(numOfThread);
    dbWork.setUseGpu(useGpu);
    crnnWork.initModel(crnn_param, crnn_bin, keys);
    crnnWork.setNumThread(numOfThread);
    crnnWork.setUseGpu(useGpu);
}


static cv::Mat getRotateCropImage(const cv::Mat &src, std::vector<cv::Point> box) {
    cv::Mat image;
    src.copyTo(image);
    std::vector<cv::Point> points = box;

    int collectX[4] = {box[0].x, box[1].x, box[2].x, box[3].x};
    int collectY[4] = {box[0].y, box[1].y, box[2].y, box[3].y};
    int left = int(*std::min_element(collectX, collectX + 4));
    int right = int(*std::max_element(collectX, collectX + 4));
    int top = int(*std::min_element(collectY, collectY + 4));
    int bottom = int(*std::max_element(collectY, collectY + 4));

    cv::Mat imgCrop;
    image(cv::Rect(left, top, right - left, bottom - top)).copyTo(imgCrop);

    for (int i = 0; i < points.size(); i++) {
        points[i].x -= left;
        points[i].y -= top;
    }

    int imgCropWidth = int(sqrt(pow(points[0].x - points[1].x, 2) +
                                pow(points[0].y - points[1].y, 2)));
    int imgCropHeight = int(sqrt(pow(points[0].x - points[3].x, 2) +
                                 pow(points[0].y - points[3].y, 2)));

    cv::Point2f ptsDst[4];
    ptsDst[0] = cv::Point2f(0., 0.);
    ptsDst[1] = cv::Point2f(imgCropWidth, 0.);
    ptsDst[2] = cv::Point2f(imgCropWidth, imgCropHeight);
    ptsDst[3] = cv::Point2f(0.f, imgCropHeight);

    cv::Point2f ptsSrc[4];
    ptsSrc[0] = cv::Point2f(points[0].x, points[0].y);
    ptsSrc[1] = cv::Point2f(points[1].x, points[1].y);
    ptsSrc[2] = cv::Point2f(points[2].x, points[2].y);
    ptsSrc[3] = cv::Point2f(points[3].x, points[3].y);

    cv::Mat M = cv::getPerspectiveTransform(ptsSrc, ptsDst);

    cv::Mat partImg;
    cv::warpPerspective(imgCrop, partImg, M,
                        cv::Size(imgCropWidth, imgCropHeight),
                        cv::BORDER_REPLICATE);

    if (float(partImg.rows) >= float(partImg.cols) * 1.5) {
        cv::Mat srcCopy = cv::Mat(partImg.rows, partImg.cols, partImg.depth());
        cv::transpose(partImg, srcCopy);
        cv::flip(srcCopy, srcCopy, 0);
        return srcCopy;
    } else {
        return partImg;
    }
}

static cv::Mat matRotateClockWise180(cv::Mat src) {
    flip(src, src, 0);
    flip(src, src, 1);
    return src;
}

static std::vector<cv::Mat> getPartImages(cv::Mat &src, std::vector<TextBox> &textBoxes) {
    std::vector<cv::Mat> partImages;
    for (int i = 0; i < textBoxes.size(); ++i) {
        cv::Mat partImg = getRotateCropImage(src, textBoxes[i].boxPoint);
        partImages.emplace_back(partImg);
    }
    return partImages;
}


OcrResult OcrLite::detect(cv::Mat &src, cv::Rect &originRect, ScaleParam &scale,
                          float boxScoreThresh, float boxThresh,
                          float unClipRatio, bool doAngle, bool mostAngle) {

    cv::Mat textBoxPaddingImg = src.clone();
    int thickness = getThickness(src);
    double startTime = getCurrentTime();
    std::vector<TextBox> textBoxes = dbWork.getTextBoxes(src, scale, boxScoreThresh, boxThresh,
                                                         unClipRatio);
    double endDbNetTime = getCurrentTime();
    double dbNetTime = endDbNetTime - startTime;

    drawTextBoxes(textBoxPaddingImg, textBoxes, thickness);

    //---------- getPartImages ----------
    std::vector<cv::Mat> partImages = getPartImages(src, textBoxes);

    std::vector<Angle> angles;
    angles = angleWork.getAngles(partImages, doAngle, mostAngle);

    //Rotate partImgs
    for (int i = 0; i < partImages.size(); ++i) {
        if (angles[i].index == 0) {
            partImages.at(i) = matRotateClockWise180(partImages[i]);
        }
    }

    std::vector<TextLine> textLines = crnnWork.getTextLines(partImages);

    std::vector<TextBlock> textBlocks;
    for (int i = 0; i < textLines.size(); ++i) {
        std::vector<cv::Point> boxPoint = std::vector<cv::Point>(4);
        int padding = originRect.x;//padding conversion
        boxPoint[0] = cv::Point(textBoxes[i].boxPoint[0].x - padding,
                                textBoxes[i].boxPoint[0].y - padding);
        boxPoint[1] = cv::Point(textBoxes[i].boxPoint[1].x - padding,
                                textBoxes[i].boxPoint[1].y - padding);
        boxPoint[2] = cv::Point(textBoxes[i].boxPoint[2].x - padding,
                                textBoxes[i].boxPoint[2].y - padding);
        boxPoint[3] = cv::Point(textBoxes[i].boxPoint[3].x - padding,
                                textBoxes[i].boxPoint[3].y - padding);
        std::string text = textLines[i].text;
        text.erase(std::remove(text.begin(), text.end(), '\r'), text.end());
        TextBlock textBlock{boxPoint, textBoxes[i].score, angles[i].index, angles[i].score,
                            angles[i].time, text, textLines[i].charScores,
                            textLines[i].time,
                            angles[i].time + textLines[i].time};
        textBlocks.emplace_back(textBlock);
    }

    double endTime = getCurrentTime();
    double fullTime = endTime - startTime;
    return OcrResult{dbNetTime, textBlocks, fullTime};
}

static cv::Mat makePadding(cv::Mat &src, const int padding) {
    if (padding <= 0) return src;
    cv::Scalar paddingScalar = {255, 255, 255};
    cv::Mat paddingSrc;
    cv::copyMakeBorder(src, paddingSrc, padding, padding, padding, padding, cv::BORDER_ISOLATED,
                       paddingScalar);
    return paddingSrc;
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
}


static ScaleParam getScaleParam(cv::Mat &src, const float scale) {
    int srcWidth = src.cols;
    int srcHeight = src.rows;
    int dstWidth = int((float) srcWidth * scale);
    int dstHeight = int((float) srcHeight * scale);
    if (dstWidth % 32 != 0) {
        dstWidth = (dstWidth / 32 - 1) * 32;
        dstWidth = (std::max)(dstWidth, 32);
    }
    if (dstHeight % 32 != 0) {
        dstHeight = (dstHeight / 32 - 1) * 32;
        dstHeight = (std::max)(dstHeight, 32);
    }
    float scaleWidth = (float) dstWidth / (float) srcWidth;
    float scaleHeight = (float) dstHeight / (float) srcHeight;
    return {srcWidth, srcHeight, dstWidth, dstHeight, scaleWidth, scaleHeight};
}

static ScaleParam getScaleParam(cv::Mat &src, const int targetSize) {
    int srcWidth, srcHeight, dstWidth, dstHeight;
    srcWidth = dstWidth = src.cols;
    srcHeight = dstHeight = src.rows;

    float ratio = 1.f;
    if (srcWidth > srcHeight) {
        ratio = float(targetSize) / float(srcWidth);
    } else {
        ratio = float(targetSize) / float(srcHeight);
    }
    dstWidth = int(float(srcWidth) * ratio);
    dstHeight = int(float(srcHeight) * ratio);
    if (dstWidth % 32 != 0) {
        dstWidth = (dstWidth / 32) * 32;
        dstWidth = (std::max)(dstWidth, 32);
    }
    if (dstHeight % 32 != 0) {
        dstHeight = (dstHeight / 32) * 32;
        dstHeight = (std::max)(dstHeight, 32);
    }
    float ratioWidth = (float) dstWidth / (float) srcWidth;
    float ratioHeight = (float) dstHeight / (float) srcHeight;
    return {srcWidth, srcHeight, dstWidth, dstHeight, ratioWidth, ratioHeight};
}

static jobject toJObject(JNIEnv *env,OcrResult ocrResult){
    jobject _map = NewMap(env);
    JMap map(env,_map);
    JLocal<jobject> dbNetTime(env,NewDouble(env,ocrResult.dbNetTime));
    JLocal<jobject> detectTime(env,NewDouble(env,ocrResult.detectTime));
    JLocal<jobject> textBlocks(env,NewList(env));
    JList textBlocks2(env,textBlocks.get());
    for (auto it = ocrResult.textBlocks.begin(); it != ocrResult.textBlocks.end(); ++it) {
        TextBlock textBlock = *it;
        jobject _item = NewMap(env);
        JMap item(env,_item);
        JLocal<jobject> boxScore(env,NewFloat(env,textBlock.boxScore));
        JLocal<jobject> angleTime(env,NewDouble(env,textBlock.angleTime));
        JLocal<jstring> text(env,NewString(env,textBlock.text.c_str()));
        JLocal<jobject> crnnTime(env,NewDouble(env,textBlock.crnnTime));
        JLocal<jobject> blockTime(env,NewDouble(env,textBlock.blockTime));
        cv::Point p0 = textBlock.boxPoint[0];
        cv::Point p2 = textBlock.boxPoint[2];
        JLocal<jobject> x1(env,NewFloat(env,p0.x));
        JLocal<jobject> y1(env,NewFloat(env,p0.y));
        JLocal<jobject> x3(env,NewFloat(env,p2.x));
        JLocal<jobject> y3(env,NewFloat(env,p2.y));
        item.putK("x1",x1.get());
        item.putK("y1",y1.get());
        item.putK("x2",x3.get());
        item.putK("y2",y3.get());
        item.putK("boxScore",boxScore.get());
        item.putK("angleTime",angleTime.get());
        item.putK("text",text.get());
        item.putK("crnnTime",crnnTime.get());
        item.putK("blockTime",blockTime.get());
        textBlocks2.add(_item);
    }
    map.putK("dbNetTime", dbNetTime.get());
    map.putK("detectTime", detectTime.get());
    map.putK("textBlocks", textBlocks.get());
    return _map;
}

jobject OcrLite::detect(JNIEnv *env, jobject input,
                        jint padding, jint maxSideLen, jfloat boxScoreThresh, jfloat boxThresh,
                        jfloat unClipRatio, jboolean doAngle, jboolean mostAngle) {
    cv::Mat imgRGBA, imgRGB, imgOut;
    bitmapToMat(env, input, imgRGBA);
    cv::cvtColor(imgRGBA, imgRGB, cv::COLOR_RGBA2RGB);
    int originMaxSide = (std::max)(imgRGB.cols, imgRGB.rows);
    int resize;
    if (maxSideLen <= 0 || maxSideLen > originMaxSide) {
        resize = originMaxSide;
    } else {
        resize = maxSideLen;
    }
    resize += 2 * padding;
    cv::Rect paddingRect(padding, padding, imgRGB.cols, imgRGB.rows);
    cv::Mat paddingSrc = makePadding(imgRGB, padding);
    //按比例缩小图像，减少文字分割时间
    ScaleParam s = getScaleParam(paddingSrc, resize);//例：按长或宽缩放 src.cols=不缩放，src.cols/2=长度缩小一半
    OcrResult ocrResult = this->detect(paddingSrc, paddingRect, s, boxScoreThresh, boxThresh,unClipRatio, doAngle, mostAngle);
    return toJObject(env,ocrResult);
}