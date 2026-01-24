//
// Created by 86152 on 2026/1/13.
//

#include "CrnnNet.h"
#include <opencv2/imgproc.hpp>
#include <cstring>
#include <string>
#include <numeric>
#include <fstream>   // 用于文件读取

static double getCurrentTime() {
    return (static_cast<double>(cv::getTickCount())) / cv::getTickFrequency() * 1000;//单位毫秒
}


CrnnNet::~CrnnNet() {
    net.clear();
}

void CrnnNet::setNumThread(int numOfThread) {
    numThread = numOfThread;
}
void CrnnNet::setUseGpu(int _useGpu) {
    useGpu = _useGpu;
}

bool CrnnNet::initModel(const char *param, const char *bin, const char* keysPath) {
    int ret_param = net.load_param(param);
    int ret_bin = net.load_model(bin);
    if (ret_param != 0 || ret_bin != 0) {
        return false;
    }
    if (keysPath == NULL || strlen(keysPath) == 0) {
        return false;
    }
    std::ifstream inFile(keysPath);
    if (!inFile.is_open()) {
        return false;
    }
    std::string line;
    while (std::getline(inFile, line)) {
        keys.emplace_back(line);
    }
    inFile.close();
    return true;
}

TextLine CrnnNet::scoreToTextLine(const float *outputData, int h, int w) {
    int keySize = keys.size();
    std::string strRes;
    std::vector<float> scores;
    int lastIndex = 0;
    int maxIndex;
    float maxValue;

    for (int i = 0; i < h; i++) {
        maxIndex = 0;
        maxValue = -1000.f;
        //do softmax
        std::vector<float> exps(w);
        for (int j = 0; j < w; j++) {
            float expSingle = exp(outputData[i * w + j]);
            exps.at(j) = expSingle;
        }
        float partition = accumulate(exps.begin(), exps.end(), 0.0);//row sum
        for (int j = 0; j < w; j++) {
            float softmax = exps[j] / partition;
            if (softmax > maxValue) {
                maxValue = softmax;
                maxIndex = j;
            }
        }
        if (maxIndex > 0 && maxIndex < keySize && (!(i > 0 && maxIndex == lastIndex))) {
            scores.emplace_back(maxValue);
            strRes.append(keys[maxIndex - 1]);
        }
        lastIndex = maxIndex;
    }
    return {strRes, scores};
}

TextLine CrnnNet::getTextLine(const cv::Mat &src) {
    float scale = (float) dstHeight / (float) src.rows;
    int dstWidth = int((float) src.cols * scale);

    cv::Mat srcResize;
    cv::resize(src, srcResize, cv::Size(dstWidth, dstHeight));

    ncnn::Mat input = ncnn::Mat::from_pixels(
            srcResize.data, ncnn::Mat::PIXEL_RGB,
            srcResize.cols, srcResize.rows);

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

    return scoreToTextLine((float *) out.data, out.h, out.w);
}

std::vector<TextLine> CrnnNet::getTextLines(std::vector<cv::Mat> &partImg) {
    int size = partImg.size();
    std::vector<TextLine> textLines(size);
    for (int i = 0; i < size; ++i) {
        //getTextLine
        double startCrnnTime = getCurrentTime();
        TextLine textLine = getTextLine(partImg[i]);
        double endCrnnTime = getCurrentTime();
        textLine.time = endCrnnTime - startCrnnTime;
        textLines[i] = textLine;
    }
    return textLines;
}