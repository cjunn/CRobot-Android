//
// Created by 86152 on 2026/1/13.
//

#ifndef CROBOT_ANGLENET_H
#define CROBOT_ANGLENET_H



#include <vector>
#include <opencv2/core.hpp>
#include <net.h>

struct Angle {
    int index;
    float score;
    double time;
};

class AngleNet {
public:
    ~AngleNet();
    void setNumThread(int numOfThread);
    void setUseGpu(int useGpu);
    bool initModel(const char *param, const char *bin);
    std::vector<Angle> getAngles(std::vector<cv::Mat> &partImgs, bool doAngle, bool mostAngle);

private:
    int numThread;
    bool useGpu;
    ncnn::Net net;
    const float meanValues[3] = {127.5, 127.5, 127.5};
    const float normValues[3] = {1.0 / 127.5, 1.0 / 127.5, 1.0 / 127.5};
    const int dstWidth = 192;
    const int dstHeight = 32;

    Angle getAngle(cv::Mat &src);
};


#endif //CROBOT_ANGLENET_H
