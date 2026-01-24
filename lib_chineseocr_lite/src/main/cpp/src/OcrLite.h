//
// Created by 86152 on 2026/1/13.
//

#ifndef CROBOT_OCRLITE_H
#define CROBOT_OCRLITE_H


#include "DbNet.h"
#include "AngleNet.h"
#include "CrnnNet.h"

struct TextBlock {
    std::vector<cv::Point> boxPoint;
    float boxScore;
    int angleIndex;
    float angleScore;
    double angleTime;
    std::string text;
    std::vector<float> charScores;
    double crnnTime;
    double blockTime;
};

struct OcrResult {
    double dbNetTime;
    std::vector<TextBlock> textBlocks;
    double detectTime;
};

class OcrLite {
public:
    OcrLite();

    void init(const char *angle_param, const char *angle_bin,
              const char *db_param, const char *db_bin,
              const char *crnn_param, const char *crnn_bin,
              const char *keys,int numOfThread,bool useGpu);
    OcrResult detect(cv::Mat &src, cv::Rect &originRect, ScaleParam &scale, float boxScoreThresh, float boxThresh,
                     float unClipRatio, bool doAngle, bool mostAngle);
    jobject detect(JNIEnv *env, jobject input,
            jint padding, jint maxSideLen, jfloat boxScoreThresh, jfloat boxThresh,
            jfloat unClipRatio, jboolean doAngle, jboolean mostAngle);

private:
    DbNet dbWork;
    AngleNet angleWork;
    CrnnNet crnnWork;
    bool useGpu;
};


#endif //CROBOT_OCRLITE_H
