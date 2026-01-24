//
// Created by 86152 on 2025/11/5.
//

#ifndef CROBOT_FEATURE_H
#define CROBOT_FEATURE_H

#include <vector>
#include <string>

namespace Image {
    static size_t computeStringColorSize(const char *color) {
        size_t result = 6;
        int c = color[result];
        while (c == '|' || c == '-') {
            result += 7;
            c = color[result];
        }
        return result;
    }

    static size_t computeStringFeaturePointCount(const char *feature, size_t size) {
        size_t index = 10;
        size_t count = 0;
        while (index < size) {
            if (feature[index] == ',') {
                count += 1;
                index += 10;
            } else if (feature[index] == '\0') {
                return count + 1;
            } else {
                index++;
            }
        }
        count = count > 0 ? count + 1 : count;
        return count;
    }

    class FeatureShiftColor {
    protected:
        std::vector<int> dxVector;
        std::vector<int> dyVector;
        std::vector<int> colorVector;
        std::vector<int> shiftVector;
        size_t mPointCount;
    public:
        FeatureShiftColor(const char *featureString, size_t featureStringSize) {
            mPointCount = computeStringFeaturePointCount(featureString, featureStringSize);
            dxVector = std::vector<int>(mPointCount);
            dyVector = std::vector<int>(mPointCount);
            colorVector = std::vector<int>(mPointCount);
            shiftVector = std::vector<int>(mPointCount);
            int nowDx, nowDy, nowColor, nowShift;
            const char *nowFeature = featureString;
            size_t pos = 0;

            for (int i = 0; i < mPointCount; i++) {
                nowDx = std::stoi(nowFeature, &pos, 10);
                nowFeature += pos + 1;
                nowDy = std::stoi(nowFeature, &pos, 10);
                nowFeature += pos + 1;
                pos = computeStringColorSize(nowFeature);
                nowColor = toIntColor(nowFeature);
                nowShift = 0;
                if (nowFeature[6] == '-') {
                    nowShift = toIntColor(nowFeature + 7);
                }
                dxVector[i] = nowDx;
                dyVector[i] = nowDy;
                colorVector[i] = nowColor;
                shiftVector[i] = nowShift;
                nowFeature += pos + 1;
            }
        }

        int equal(Bitmap *bitmap, int x, int y, double sim) {
            for (int i = 0; i < mPointCount; i++) {
                int mColor = colorVector[i];
                int mShift = shiftVector[i];
                int nowX = x + dxVector[i];
                int nowY = y + dyVector[i];
                isInBitmapScope(bitmap, nowX, nowY);
                if (!compareColor(computeCoordColor(bitmap, nowX, nowY),
                                  (unsigned char *) &mColor,
                                  (unsigned char *) &mShift, sim)) {
                    return 0;
                }
            }
            return 1;
        }
    };

    class NoFeatureShiftColor : FeatureShiftColor {
    protected:
        size_t toleranceValue;
    public:
        NoFeatureShiftColor(const char *feature, size_t featureSize, double tolerance)
                : FeatureShiftColor(feature, featureSize) {
            toleranceValue = this->mPointCount * tolerance;
        }

        int equal(Bitmap *bitmap, int x, int y, double sim) {
            size_t currentTolerance = 0;
            for (int i = 0; i < mPointCount; i++) {
                int mColor = colorVector[i];
                int mShift = shiftVector[i];
                int nowX = x + dxVector[i];
                int nowY = y + dyVector[i];
                isInBitmapScope(bitmap, nowX, nowY);
                if (compareColor(computeCoordColor(bitmap, nowX, nowY),
                                 (unsigned char *) &mColor,
                                 (unsigned char *) &mShift, sim)) {
                    currentTolerance++;
                    if (currentTolerance >= toleranceValue) {
                        return 0;
                    }
                }
            }
            return 1;
        }
    };

    class ComplexFeatureShiftColor {
        FeatureShiftColor *mFeature;
        NoFeatureShiftColor *mNoFeature;
    public:
        ComplexFeatureShiftColor(FeatureShiftColor *mFeature,
                                 NoFeatureShiftColor *mNoFeature) : mFeature(mFeature),
                                                                    mNoFeature(mNoFeature) {
        }

        int equal(Bitmap *bitmap, int x, int y, double sim) {
            if (mFeature->equal(bitmap, x, y, sim)) {
                if (mNoFeature->equal(bitmap, x, y, sim)) {
                    return 1;
                }
            }
            return 0;
        }
    };

} // Image

#endif //CROBOT_FEATURE_H
