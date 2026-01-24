// Tencent is pleased to support the open source community by making ncnn available.
//
// Copyright (C) 2024 THL A29 Limited, a Tencent company. All rights reserved.
//
// Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
// https://opensource.org/licenses/BSD-3-Clause
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.

// 1. install
//      pip3 install -U ultralytics pnnx ncnn
// 2. export yolov8-cls torchscript
//      yolo export model=yolov8n-cls.pt format=torchscript
// 3. convert torchscript with static shape
//      pnnx yolov8n-cls.torchscript
// 4. now you get ncnn model files
//      yolov8n_cls.ncnn.param
//      yolov8n_cls.ncnn.bin

#include "yolov8.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#include <float.h>
#include <stdio.h>
#include <vector>

static void get_topk(const ncnn::Mat& cls_scores, int topk, std::vector<Object>& objects)
{
    // partial sort topk with index
    int size = cls_scores.w;
    std::vector<std::pair<float, int> > vec;
    vec.resize(size);
    for (int i = 0; i < size; i++)
    {
        vec[i] = std::make_pair(cls_scores[i], i);
    }

    std::partial_sort(vec.begin(), vec.begin() + topk, vec.end(),
                      std::greater<std::pair<float, int> >());

    objects.resize(topk);
    for (int i = 0; i < topk; i++)
    {
        objects[i].label = vec[i].second;
        objects[i].prob = vec[i].first;
    }
}

int YOLOv8_cls::detect(const cv::Mat& rgb, std::vector<Object>& objects)
{
    const int target_size = 224;
    const int topk = 5;

    int img_w = rgb.cols;
    int img_h = rgb.rows;

    // letterbox pad
    int w = img_w;
    int h = img_h;
    float scale = 1.f;
    if (w > h)
    {
        scale = (float)target_size / w;
        w = target_size;
        h = h * scale;
    }
    else
    {
        scale = (float)target_size / h;
        h = target_size;
        w = w * scale;
    }

    ncnn::Mat in = ncnn::Mat::from_pixels_resize(rgb.data, ncnn::Mat::PIXEL_RGB, img_w, img_h, w, h);

    // letterbox pad to target_size rectangle
    int wpad = target_size - w;
    int hpad = target_size - h;
    ncnn::Mat in_pad;
    ncnn::copy_make_border(in, in_pad, hpad / 2, hpad - hpad / 2, wpad / 2, wpad - wpad / 2, ncnn::BORDER_CONSTANT, 114.f);

    const float norm_vals[3] = {1 / 255.f, 1 / 255.f, 1 / 255.f};
    in_pad.substract_mean_normalize(0, norm_vals);

    ncnn::Extractor ex = yolov8.create_extractor();

    ex.input("in0", in_pad);

    ncnn::Mat out;
    ex.extract("out0", out);

    // return top-5
    get_topk(out, topk, objects);

    return 0;
}

