//
// Created by 86152 on 2026/1/13.
//

#ifndef CROBOT_CRNNNET_H
#define CROBOT_CRNNNET_H

#include <vector>
#include <opencv2/core.hpp>
#include <net.h>

struct TextLine {
    std::string text;
    std::vector<float> charScores;
    double time;
};

class CrnnNet {
public:
    ~CrnnNet();
    void setNumThread(int numOfThread);
    void setUseGpu(int useGpu);
    bool initModel(const char *param, const char *bin,const char* keysPath);
    std::vector<TextLine> getTextLines(std::vector<cv::Mat> &partImg);

private:
    int numThread;
    bool useGpu;
    ncnn::Net net;

    const float meanValues[3] = {127.5, 127.5, 127.5};
    const float normValues[3] = {1.0 / 127.5, 1.0 / 127.5, 1.0 / 127.5};
    const int dstHeight = 32;

    std::vector<std::string> keys;

    TextLine scoreToTextLine(const float *outputData, int h, int w);

    TextLine getTextLine(const cv::Mat &src);
};



#endif //CROBOT_CRNNNET_H
