//
// Created by 86152 on 2025/11/5.
//

#ifndef CROBOT_FINDER_H
#define CROBOT_FINDER_H

#include "color.h"
#include "image.h"
#include "feature.h"

namespace Image {
    static class ColorFinder {
        MulShiftColor *mMulShiftColor;
        double mSim;
        Point mPoint;
    public:
        ColorFinder(MulShiftColor *mulShiftColor, double sim) : mMulShiftColor(mulShiftColor),
                                                                mSim(sim) {
        }

        bool compare(int x, int y, const unsigned char *color) {
            if (mMulShiftColor->equal(color, mSim)) {
                mPoint.x = x;
                mPoint.y = y;
                return true;
            }
            return false;
        }

        Point &getResult() {
            return mPoint;
        }
    };

    static class ComplexFeatureFinder {
        Bitmap *mBitmap;
        ShiftColor *mFirstShiftColor;
        ComplexFeatureShiftColor *mFeature;
        double mSim;
        Point mPoint;
    public:
        ComplexFeatureFinder(Bitmap *bitmap, ShiftColor *firstShiftColor,
                             ComplexFeatureShiftColor *feature, double sim) : mBitmap(bitmap),
                                                                              mFirstShiftColor(
                                                                                      firstShiftColor),
                                                                              mFeature(feature),
                                                                              mSim(sim) {
        }

        bool compare(int x, int y, const unsigned char *color) {
            if (mFirstShiftColor->equal(color, mSim) && mFeature->equal(mBitmap, x, y, mSim)) {
                mPoint.x = x;
                mPoint.y = y;
                return true;
            }
            return false;
        }

        Point &getResult() {
            return mPoint;
        }
    };

    static class FeatureFinder {
        Bitmap *mBitmap;
        ShiftColor *mFirstShiftColor;
        FeatureShiftColor *mFeature;
        double mSim;
        Point mPoint;
    public:
        FeatureFinder(Bitmap *bitmap, ShiftColor *firstShiftColor, FeatureShiftColor *feature,
                      double sim)
                : mBitmap(bitmap), mFirstShiftColor(firstShiftColor), mFeature(feature), mSim(sim) {
        }

        bool compare(int x, int y, const unsigned char *color) {
            if (mFirstShiftColor->equal(color, mSim) && mFeature->equal(mBitmap, x, y, mSim)) {
                mPoint.x = x;
                mPoint.y = y;
                return true;
            }
            return false;
        }

        Point &getResult() {
            return mPoint;
        }
    };

    SimpleColor getSimpleColor(Bitmap *bitmap, int x, int y) {
        SimpleColor simpleColor;
        isInBitmapScope(bitmap,x,y);
        Pixel *pixel = (Pixel *) &simpleColor;
        const unsigned char *c = computeCoordColor(bitmap, x, y);
        pixel->g = c[1];
        pixel->r = c[2];
        pixel->b = c[0];
        return simpleColor;
    }

    bool findColor(Bitmap *bitmap, int x, int y, int x1, int y1, MulShiftColor *mulShiftColor,
                   double sim, int order, Point *out) {
        ColorFinder finder(mulShiftColor, sim);
        bool result = orderFindColor(bitmap, x, y, x1, y1, order, &finder);
        if (result && out) {
            Point &point = finder.getResult();
            out->x = point.x;
            out->y = point.y;
        }
        return result;
    }

    void findFeature(Bitmap *bitmap, int x, int y, int x1, int y1, ShiftColor *shiftColor,
                     FeatureShiftColor *feature, double sim, int order, Point *out) {
        FeatureFinder finder(bitmap, shiftColor, feature, sim);
        if (orderFindColor(bitmap, x, y, x1, y1, order, &finder)) {
            Point &point = finder.getResult();
            out->x = point.x;
            out->y = point.y;
        }
    }

    void findComplexFeature(Bitmap *bitmap, int x, int y, int x1, int y1, ShiftColor *shiftColor,
                            ComplexFeatureShiftColor *feature, double sim, int order, Point *out) {
        ComplexFeatureFinder finder(bitmap, shiftColor, feature, sim);
        if (orderFindColor(bitmap, x, y, x1, y1, order, &finder)) {
            Point &point = finder.getResult();
            out->x = point.x;
            out->y = point.y;
        }
    }

} // Image

#endif //CROBOT_FINDER_H
