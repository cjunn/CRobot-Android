//
// Created by 86152 on 2025/11/5.
//

#ifndef CROBOT_COLOR_H
#define CROBOT_COLOR_H

#include <vector>
#include "image.h"

namespace Image {
    static size_t computePointCount(const char *feature, size_t size) {
        size_t index = 6;
        size_t count = 0;
        while (index < size) {
            if (feature[index] == '|') {
                count += 1;
                index += 6;
            } else if (feature[index] == '\0') {
                return count + 1;
            } else {
                index++;
            }
        }
        return count;
    }

    class SimpleColor {
    protected:
        int mColor;
    public:

        SimpleColor() : mColor(0) {

        }

        SimpleColor(int intColor) : mColor(intColor) {

        }

        SimpleColor(const unsigned char *strColor) : mColor(0) {
            memcpy(&mColor, strColor, 3);
        }

        operator int() {
            return mColor;
        }

        int operator==(const SimpleColor &other) {
            return mColor == other.mColor;
        }

        int operator==(const int other) {
            return mColor == other;
        }

        int equal(int color) {
            return mColor == color;
        }

        int equal(const unsigned char *color,
                  const unsigned char *shiftColor,
                  double sim) {
            return compareColor((const unsigned char *) &mColor, color, shiftColor, sim);
        }

        void toChar(char* p){
            toCharColor(mColor,p);
        }

    };

    class ShiftColor {
    protected:
        int mShift = 0;
        int mColor = 0;
    public:
        ShiftColor(const char *str) {
            mColor = toIntColor(str);
            if (str[6] == '-') {
                mShift = toIntColor(str + 7);
            }
        }

        int equal(const unsigned char *color, double sim) {
            return compareColor(color, (const unsigned char *) &mColor,
                                (const unsigned char *) &mShift, sim);
        }
    };

    class MulShiftColor {
    protected:
        std::vector<int> colorVector;
        std::vector<int> shiftVector;
        size_t mPointCount;
    public:
        MulShiftColor(const char *str, size_t strSize) {
            mPointCount = computePointCount(str, strSize);
            colorVector = std::vector<int>(mPointCount);
            shiftVector = std::vector<int>(mPointCount);
            size_t nowPointCount = 1;
            int idx = 0;
            while (nowPointCount++ <= mPointCount) {
                colorVector[idx] = toIntColor(str);
                if (str[6] == '-') {
                    str = str + 7;
                    shiftVector[idx] = toIntColor(str);
                } else {
                    str = str + 1;
                    shiftVector[idx] = 0;
                }
                idx++;
            }
        }

        int equal(const unsigned char *color, double sim) {
            for (int i = 0; i < mPointCount; i++) {
                int mColor = colorVector[i];
                int mShift = shiftVector[i];
                if (compareColor(color, (const unsigned char *) &mColor,
                                 (const unsigned char *) &mShift, sim)) {
                    return i;
                }
            }
            return -1;
        }
    };

} // Image

#endif //CROBOT_COLOR_H
