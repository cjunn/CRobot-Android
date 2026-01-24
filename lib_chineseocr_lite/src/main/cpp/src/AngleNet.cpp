//
// Created by 86152 on 2026/1/13.
//

#include <opencv2/imgproc.hpp>
#include <numeric>
#include "AngleNet.h"
#include "jvm.h"

AngleNet::~AngleNet() {
    net.clear();
}

void AngleNet::setNumThread(int numOfThread) {
    numThread = numOfThread;
}
void AngleNet::setUseGpu(int _useGpu) {
    useGpu = _useGpu;
}

bool AngleNet::initModel(const char *param, const char *bin) {
    int ret_param = net.load_param(param);
    int ret_bin = net.load_model(bin);
    if (ret_param != 0 || ret_bin != 0) {
        return false;
    }
    return true;
}

static Angle scoreToAngle(const float *outputData, int w) {
    int maxIndex = 0;
    float maxScore = -1000.0f;
    for (int i = 0; i < w; i++) {
        if (i == 0)maxScore = outputData[i];
        else if (outputData[i] > maxScore) {
            maxScore = outputData[i];
            maxIndex = i;
        }
    }
    return {maxIndex, maxScore};
}

static double getCurrentTime() {
    return (static_cast<double>(cv::getTickCount())) / cv::getTickFrequency() * 1000;//单位毫秒
}

static cv::Mat adjustTargetImg(cv::Mat &src, int dstWidth, int dstHeight) {
    cv::Mat srcResize;
    float scale = (float) dstHeight / (float) src.rows;
    int angleWidth = int((float) src.cols * scale);
    cv::resize(src, srcResize, cv::Size(angleWidth, dstHeight));
    cv::Mat srcFit = cv::Mat(dstHeight, dstWidth, CV_8UC3, cv::Scalar(255, 255, 255));
    if (angleWidth < dstWidth) {
        cv::Rect rect(0, 0, srcResize.cols, srcResize.rows);
        srcResize.copyTo(srcFit(rect));
    } else {
        cv::Rect rect(0, 0, dstWidth, dstHeight);
        srcResize(rect).copyTo(srcFit);
    }
    return srcFit;
}


static std::vector<int> getAngleIndexes(std::vector<Angle> &angles) {
    std::vector<int> angleIndexes;
    angleIndexes.reserve(angles.size());
    for (int i = 0; i < angles.size(); ++i) {
        angleIndexes.push_back(angles[i].index);
    }
    return angleIndexes;
}


Angle AngleNet::getAngle(cv::Mat &src) {
    ncnn::Mat input = ncnn::Mat::from_pixels(src.data, ncnn::Mat::PIXEL_RGB,src.cols, src.rows);
    input.substract_mean_normalize(meanValues, normValues);
    ncnn::Extractor extractor = net.create_extractor();
    extractor.set_num_threads(numThread);
    #if NCNN_VULKAN
    if(useGpu){
        net.opt.use_vulkan_compute = useGpu;
    }
    #endif

    extractor.input("input", input);
    ncnn::Mat out;
    extractor.extract("out", out);
    return scoreToAngle((float *) out.data, out.w);
}


std::vector<Angle> AngleNet::getAngles(std::vector<cv::Mat> &partImgs, bool doAngle, bool mostAngle) {
    int size = partImgs.size();
    std::vector<Angle> angles(size);
    if (doAngle) {
        for (int i = 0; i < size; ++i) {
            double startAngle = getCurrentTime();
            auto angleImg = adjustTargetImg(partImgs[i], dstWidth, dstHeight);
            Angle angle = getAngle(angleImg);
            double endAngle = getCurrentTime();
            angle.time = endAngle - startAngle;
            angles[i] = angle;
        }
    } else {
        for (int i = 0; i < size; ++i) {
            angles[i] = Angle{-1, 0.f};
        }
    }
    //Most Possible AngleIndex
    if (doAngle && mostAngle) {
        auto angleIndexes = getAngleIndexes(angles);
        double sum = std::accumulate(angleIndexes.begin(), angleIndexes.end(), 0.0);
        double halfPercent = angles.size() / 2.0f;
        int mostAngleIndex;
        if (sum < halfPercent) {//all angle set to 0
            mostAngleIndex = 0;
        } else {//all angle set to 1
            mostAngleIndex = 1;
        }
        for (int i = 0; i < angles.size(); ++i) {
            Angle angle = angles[i];
            angle.index = mostAngleIndex;
            angles.at(i) = angle;
        }
    }

    return angles;
}