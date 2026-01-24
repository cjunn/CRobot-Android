//
// Created by 86152 on 2026/1/13.
//

#ifndef CROBOT_DBNET_H
#define CROBOT_DBNET_H
#include <vector>
#include <opencv2/core.hpp>
#include <net.h>
struct ScaleParam {
    int srcWidth;
    int srcHeight;
    int dstWidth;
    int dstHeight;
    float ratioWidth;
    float ratioHeight;
};

struct TextBox {
    std::vector<cv::Point> boxPoint;
    float score;
};


class DbNet {
public:
    DbNet();
    ~DbNet();
    void setNumThread(int numOfThread);
    void setUseGpu(int useGpu);
    bool initModel(const char *param, const char *bin);
    std::vector<TextBox> getTextBoxes(cv::Mat &src, ScaleParam &s, float boxScoreThresh,
                                      float boxThresh, float unClipRatio);
private:
    int numThread;
    bool useGpu;
    ncnn::Net net;
    const float meanValues[3] = {0.485 * 255, 0.456 * 255, 0.406 * 255};
    const float normValues[3] = {1.0 / 0.229 / 255.0, 1.0 / 0.224 / 255.0, 1.0 / 0.225 / 255.0};
};


#endif //CROBOT_DBNET_H
