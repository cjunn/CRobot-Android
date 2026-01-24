//
// Created by 86152 on 2025/12/18.
//

#include "image.h"
#include <cmath>
#include <vector>
#include <string>



static size_t ComputePointCount(const char *feature, size_t size) {
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
static int ComputeCharBit(char c) {
    if (c <= '9')
        c -= '0';
    else if (c <= 'F')
        c -= 55;
    else
        c -= 87;
    return c;
}
static char ComputeIntBit(int c) {
    if (c <= 9)
        return c + '0';
    else
        return c + 55;
}
static void SwapPosition(int &p1, int &p2) {
    if (p1 > p2) {
        int tmp;
        p1 = tmp;
        p1 = p2;
        p2 = tmp;
    }
}
static void LimitedScope(int &p, int &scope) {
    if (p < 0) {
        p = 0;
    } else if (p >= scope) {
        p = scope - 1;
    }
}
static int CompareColor(const unsigned char *color, const unsigned char *color1,const unsigned char *shift, double sim) {
    int r0 = abs(color1[0] - color[0]);
    int r1 = abs(color1[1] - color[1]);
    int r2 = abs(color1[2] - color[2]);
    int tr = r0 + r1 + r2;
    return ((double) 1 - (double) tr / (double) 765 >= sim) ||
           (r0 <= shift[0] && r1 <= shift[1] && r2 <= shift[2]);
}
static int ToIntColor(const char *s) {
    int r = 0;
    r |= (ComputeCharBit(s[0]) << 20);
    r |= (ComputeCharBit(s[1]) << 16);
    r |= (ComputeCharBit(s[2]) << 12);
    r |= (ComputeCharBit(s[3]) << 8);
    r |= (ComputeCharBit(s[4]) << 4);
    r |= (ComputeCharBit(s[5]) << 0);
    return r;
}
static bool IsInBitmapScope(Bitmap *bitmap, int &x, int &y) {
    LimitedScope(x, bitmap->width);
    LimitedScope(y, bitmap->height);
    return true;
}
static bool IsInBitmapScope(Bitmap *bitmap, int &x, int &y, int &x1, int &y1) {
    LimitedScope(x, bitmap->width);
    LimitedScope(x1, bitmap->width);
    LimitedScope(y, bitmap->height);
    LimitedScope(y1, bitmap->height);
    SwapPosition(x, x1);
    SwapPosition(y, y1);
    return true;
}
static unsigned char *ComputeCoordColor(Bitmap *bitmap, int x, int y) {
    return bitmap->origin + y * bitmap->rowShift + x * bitmap->pixelStride;
}
static void ToCharColor(const int s, char *out) {
    out[0] = ComputeIntBit((s >> 20) & 15);
    out[1] = ComputeIntBit((s >> 16) & 15);
    out[2] = ComputeIntBit((s >> 12) & 15);
    out[3] = ComputeIntBit((s >> 8) & 15);
    out[4] = ComputeIntBit((s >> 4) & 15);
    out[5] = ComputeIntBit((s >> 0) & 15);
    out[6] = '\0';
}
static int ShiftColorDiff(char* color1,char* color){
    int r0 = abs(color1[0] - color[0]);
    int r1 = abs(color1[1] - color[1]);
    int r2 = abs(color1[2] - color[2]);
    return r0 + r1 + r2;
}
static size_t ComputeStringColorSize(const char *color) {
    size_t result = 6;
    int c = color[result];
    while (c == '|' || c == '-') {
        result += 7;
        c = color[result];
    }
    return result;
}
static size_t ComputeStringFeaturePointCount(const char *feature, size_t size) {
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

static class ShiftColor {
protected:
    int mShift = 0;
    int mColor = 0;
public:
    ShiftColor(const char *str) {
        mColor = ToIntColor(str);
        if (str[6] == '-') {
            mShift = ToIntColor(str + 7);
        }
    }

    int equal(const unsigned char *color, double sim) {
        return CompareColor(color, (const unsigned char *) &mColor,(const unsigned char *) &mShift, sim);
    }
};
static class MulShiftColor {
protected:
    std::vector<int> colorVector;
    std::vector<int> shiftVector;
    size_t mPointCount;
public:
    MulShiftColor(const char *str, size_t strSize) {
        mPointCount = ComputePointCount(str, strSize);
        colorVector = std::vector<int>(mPointCount);
        shiftVector = std::vector<int>(mPointCount);
        size_t nowPointCount = 1;
        int idx = 0;
        while (nowPointCount++ <= mPointCount) {
            colorVector[idx] = ToIntColor(str);
            if (str[6] == '-') {
                str = str + 7;
                shiftVector[idx] = ToIntColor(str);
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
            if (CompareColor(color, (const unsigned char *) &mColor,(const unsigned char *) &mShift, sim)) {
                return i;
            }
        }
        return -1;
    }
};
static class FeatureShiftColor {
protected:
    std::vector<int> dxVector;
    std::vector<int> dyVector;
    std::vector<int> colorVector;
    std::vector<int> shiftVector;
    size_t mPointCount;
public:
    FeatureShiftColor(const char *featureString, size_t featureStringSize) {
        mPointCount = ComputeStringFeaturePointCount(featureString, featureStringSize);
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
            pos = ComputeStringColorSize(nowFeature);
            nowColor = ToIntColor(nowFeature);
            nowShift = 0;
            if (nowFeature[6] == '-') {
                nowShift = ToIntColor(nowFeature + 7);
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
            IsInBitmapScope(bitmap, nowX, nowY);
            if (!CompareColor(ComputeCoordColor(bitmap, nowX, nowY),(unsigned char *) &mColor,(unsigned char *) &mShift, sim)) {
                return 0;
            }
        }
        return 1;
    }
};
static class NoFeatureShiftColor : FeatureShiftColor {
protected:
    size_t toleranceValue;
public:
    NoFeatureShiftColor(const char *feature, size_t featureSize, double tolerance): FeatureShiftColor(feature, featureSize) {
        toleranceValue = this->mPointCount * tolerance;
    }

    int equal(Bitmap *bitmap, int x, int y, double sim) {
        size_t currentTolerance = 0;
        for (int i = 0; i < mPointCount; i++) {
            int mColor = colorVector[i];
            int mShift = shiftVector[i];
            int nowX = x + dxVector[i];
            int nowY = y + dyVector[i];
            IsInBitmapScope(bitmap, nowX, nowY);
            if (CompareColor(ComputeCoordColor(bitmap, nowX, nowY),(unsigned char *) &mColor,(unsigned char *) &mShift, sim)) {
                currentTolerance++;
                if (currentTolerance >= toleranceValue) {
                    return 0;
                }
            }
        }
        return 1;
    }
};
static class ComplexFeatureShiftColor {
    FeatureShiftColor *mFeature;
    NoFeatureShiftColor *mNoFeature;
public:
    ComplexFeatureShiftColor(FeatureShiftColor *mFeature,NoFeatureShiftColor *mNoFeature) : mFeature(mFeature),mNoFeature(mNoFeature) {
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

static class BaseFinder {
public:
    virtual bool compare(int x, int y, const unsigned char *color) = 0;
    virtual Point &getResult() = 0;
};
static class ColorFinder: public BaseFinder{
    MulShiftColor *mMulShiftColor;
    double mSim;
    Point mPoint;
public:
    ColorFinder(MulShiftColor *mulShiftColor, double sim) : mMulShiftColor(mulShiftColor), mSim(sim) {
    }
    bool compare(int x, int y, const unsigned char *color) override {
        if (mMulShiftColor->equal(color, mSim)) {
            mPoint.x = x;
            mPoint.y = y;
            return true;
        }
        return false;
    }
    Point &getResult() override {
        return mPoint;
    }
};
static class ComplexFeatureFinder : public BaseFinder{
    Bitmap *mBitmap;
    ShiftColor *mFirstShiftColor;
    ComplexFeatureShiftColor *mFeature;
    double mSim;
    Point mPoint;
public:
    ComplexFeatureFinder(Bitmap *bitmap, ShiftColor *firstShiftColor,ComplexFeatureShiftColor *feature, double sim) : mBitmap(bitmap),
                                                                                                                      mFirstShiftColor(firstShiftColor),mFeature(feature),mSim(sim) {
    }
    bool compare(int x, int y, const unsigned char *color) override {
        if (mFirstShiftColor->equal(color, mSim) && mFeature->equal(mBitmap, x, y, mSim)) {
            mPoint.x = x;
            mPoint.y = y;
            return true;
        }
        return false;
    }
    Point &getResult() override {
        return mPoint;
    }
};
static class FeatureFinder : public BaseFinder{
    Bitmap *mBitmap;
    ShiftColor *mFirstShiftColor;
    FeatureShiftColor *mFeature;
    double mSim;
    Point mPoint;
public:
    FeatureFinder(Bitmap *bitmap, ShiftColor *firstShiftColor, FeatureShiftColor *feature,double sim): mBitmap(bitmap), mFirstShiftColor(firstShiftColor), mFeature(feature), mSim(sim) {
    }

    bool compare(int x, int y, const unsigned char *color) override {
        if (mFirstShiftColor->equal(color, mSim) && mFeature->equal(mBitmap, x, y, mSim)) {
            mPoint.x = x;
            mPoint.y = y;
            return true;
        }
        return false;
    }

    Point &getResult() override {
        return mPoint;
    }
};

static bool UpDownLeftRightReadColor(Bitmap *bitmap, int x, int y, int x1, int y1, BaseFinder *comparator) {
    const unsigned char *moveVerticalPointer;
    const unsigned char *moveLinePointer = bitmap->origin + y * bitmap->rowShift + x * bitmap->pixelStride;
    for (int ix = x; ix <= x1; ix++) {
        moveVerticalPointer = moveLinePointer;
        for (int iy = y; iy <= y1; iy++) {
            if (comparator->compare(ix, iy, moveVerticalPointer))
                return true;
            moveVerticalPointer += bitmap->rowShift;
        }
        moveLinePointer += bitmap->pixelStride;
    }
    return false;
}
static bool UpDownRightLeftReadColor(Bitmap *bitmap, int x, int y, int x1, int y1, BaseFinder *comparator) {
    const unsigned char *moveVerticalPointer;
    const unsigned char *moveLinePointer = bitmap->origin + y * bitmap->rowShift + x1 * bitmap->pixelStride;
    for (int ix = x1; ix >= x; ix--) {
        moveVerticalPointer = moveLinePointer;
        for (int iy = y; iy <= y1; iy++) {
            if (comparator->compare(ix, iy, moveVerticalPointer))
                return true;
            moveVerticalPointer += bitmap->rowShift;
        }
        moveLinePointer -= bitmap->pixelStride;
    }
    return false;
}
static bool DownUpLeftRightReadColor(Bitmap *bitmap, int x, int y, int x1, int y1, BaseFinder *comparator) {
    const unsigned char *moveVerticalPointer;
    const unsigned char *moveLinePointer = bitmap->origin + y1 * bitmap->rowShift + x * bitmap->pixelStride;
    for (int ix = x; ix <= x1; ix++) {
        moveVerticalPointer = moveLinePointer;
        for (int iy = y1; iy >= y; iy--) {
            if (comparator->compare(ix, iy, moveVerticalPointer))
                return true;
            moveVerticalPointer -= bitmap->rowShift;
        }
        moveLinePointer += bitmap->pixelStride;
    }
    return false;
}
static bool DownUpRightLeftReadColor(Bitmap *bitmap, int x, int y, int x1, int y1, BaseFinder *comparator) {
    const unsigned char *moveVerticalPointer;
    const unsigned char *moveLinePointer = bitmap->origin + y1 * bitmap->rowShift + x1 * bitmap->pixelStride;
    for (int ix = x1; ix >= x; ix--) {
        moveVerticalPointer = moveLinePointer;
        for (int iy = y1; iy >= y; iy--) {
            if (comparator->compare(ix, iy, moveVerticalPointer))
                return true;
            moveVerticalPointer -= bitmap->rowShift;
        }
        moveLinePointer -= bitmap->pixelStride;
    }
    return false;
}
static bool LeftRightUpDownReadColor(Bitmap *bitmap, int x, int y, int x1, int y1, BaseFinder *comparator) {
    const unsigned char *moveLinePointer;
    const unsigned char *moveVerticalPointer = bitmap->origin + y * bitmap->rowShift + x * bitmap->pixelStride;
    for (int iy = y; iy <= y1; iy++) {
        moveLinePointer = moveVerticalPointer;
        for (int ix = x; ix <= x1; ix++) {
            if (comparator->compare(ix, iy, moveLinePointer))
                return true;
            moveLinePointer += bitmap->pixelStride;
        }
        moveVerticalPointer += bitmap->rowShift;
    }
    return false;
}
static bool LeftRightDownUpReadColor(Bitmap *bitmap, int x, int y, int x1, int y1, BaseFinder *comparator) {
    const unsigned char *moveLinePointer;
    const unsigned char *moveVerticalPointer = bitmap->origin + y1 * bitmap->rowShift + x * bitmap->pixelStride;
    for (int iy = y1; iy >= y; iy--) {
        moveLinePointer = moveVerticalPointer;
        for (int ix = x; ix <= x1; ix++) {
            if (comparator->compare(ix, iy, moveLinePointer))
                return true;
            moveLinePointer += bitmap->pixelStride;
        }
        moveVerticalPointer -= bitmap->rowShift;
    }
    return false;
}
static bool RightLeftUpDownReadColor(Bitmap *bitmap, int x, int y, int x1, int y1, BaseFinder *comparator) {
    const unsigned char *moveLinePointer;
    const unsigned char *moveVerticalPointer = bitmap->origin + y * bitmap->rowShift + x1 * bitmap->pixelStride;
    for (int iy = y; iy <= y1; iy++) {
        moveLinePointer = moveVerticalPointer;
        for (int ix = x1; ix >= x; ix--) {
            if (comparator->compare(ix, iy, moveLinePointer))
                return true;
            moveLinePointer -= bitmap->pixelStride;
        }
        moveVerticalPointer += bitmap->rowShift;
    }
    return false;
}
static bool RightLeftDownUpReadColor(Bitmap *bitmap, int x, int y, int x1, int y1, BaseFinder *comparator) {
    const unsigned char *moveLinePointer;
    const unsigned char *moveVerticalPointer = bitmap->origin + y1 * bitmap->rowShift + x1 * bitmap->pixelStride;
    for (int iy = y1; iy >= y; iy--) {
        moveLinePointer = moveVerticalPointer;
        for (int ix = x1; ix >= x; ix--) {
            if (comparator->compare(ix, iy, moveLinePointer))
                return true;
            moveLinePointer -= bitmap->pixelStride;
        }
        moveVerticalPointer -= bitmap->rowShift;
    }
    return false;
}
static bool OrderFindColor(Bitmap *bitmap, int x, int y, int x1, int y1, int readOrder, BaseFinder *comparator) {
    switch (readOrder) {
        case UP_DOWN_LEFT_RIGHT:
            return UpDownLeftRightReadColor(bitmap, x, y, x1, y1, comparator);
        case UP_DOWN_RIGHT_LEFT:
            return UpDownRightLeftReadColor(bitmap, x, y, x1, y1, comparator);
        case DOWN_UP_LEFT_RIGHT:
            return DownUpLeftRightReadColor(bitmap, x, y, x1, y1, comparator);
        case DOWN_UP_RIGHT_LEFT:
            return DownUpRightLeftReadColor(bitmap, x, y, x1, y1, comparator);
        case LEFT_RIGHT_UP_DOWN:
            return LeftRightUpDownReadColor(bitmap, x, y, x1, y1, comparator);
        case RIGHT_LEFT_UP_DOWN:
            return RightLeftUpDownReadColor(bitmap, x, y, x1, y1, comparator);
        case LEFT_RIGHT_DOWN_UP:
            return LeftRightDownUpReadColor(bitmap, x, y, x1, y1, comparator);
        case RIGHT_LEFT_DOWN_UP:
            return RightLeftDownUpReadColor(bitmap, x, y, x1, y1, comparator);
    }
    return false;
}
static bool FindColor(Bitmap *bitmap, int x, int y, int x1, int y1, MulShiftColor *mulShiftColor,double sim, int order, Point *out) {
    ColorFinder finder(mulShiftColor, sim);
    bool result = OrderFindColor(bitmap, x, y, x1, y1, order, &finder);
    if (result && out) {
        Point &point = finder.getResult();
        out->x = point.x;
        out->y = point.y;
    }
    return result;
}
static void FindFeature(Bitmap *bitmap, int x, int y, int x1, int y1, ShiftColor *shiftColor,FeatureShiftColor *feature, double sim, int order, Point *out) {
    FeatureFinder finder(bitmap, shiftColor, feature, sim);
    if (OrderFindColor(bitmap, x, y, x1, y1, order, &finder)) {
        Point &point = finder.getResult();
        out->x = point.x;
        out->y = point.y;
    }
}
static void FindComplexFeature(Bitmap *bitmap, int x, int y, int x1, int y1, ShiftColor *shiftColor,ComplexFeatureShiftColor *feature, double sim, int order, Point *out) {
    ComplexFeatureFinder finder(bitmap, shiftColor, feature, sim);
    if (OrderFindColor(bitmap, x, y, x1, y1, order, &finder)) {
        Point &point = finder.getResult();
        out->x = point.x;
        out->y = point.y;
    }
}

void GetSimpleColor(Bitmap *bitmap, int x, int y, Color *out) {
    IsInBitmapScope(bitmap,x,y);
    Pixel *pixel = (Pixel *) out;
    const unsigned char *c = ComputeCoordColor(bitmap, x, y);
    pixel->g = c[1];
    pixel->r = c[2];
    pixel->b = c[0];
}
bool FindColor(Bitmap *bitmap, int x, int y, int x1, int y1,const char *mulShift, double sim, int order, Point *out) {
    MulShiftColor mul(mulShift, strlen(mulShift));
    FindColor(bitmap,x,y,x1,y1,&mul,sim,order,out);
}
void FindFeature(Bitmap *bitmap, int x, int y, int x1, int y1,const char *first,const char *feature, double sim, int order, Point *out) {
    FeatureShiftColor featureShiftColor(feature, strlen(feature));
    ShiftColor firstShiftColor(first);
    FindFeature(bitmap, x, y, x1, y1, &firstShiftColor, &featureShiftColor, sim, order, out);
}
void FindComplexFeature(Bitmap *bitmap, int x, int y, int x1, int y1, const char *first, const char *featureString, const char *noFeatureString, double sim, double tolerance, int order, Point *out) {
    ShiftColor firstShiftColor(first);
    FeatureShiftColor featureShiftColor(featureString, strlen(featureString));
    NoFeatureShiftColor noFeatureShiftColor(noFeatureString, strlen(noFeatureString), tolerance);
    ComplexFeatureShiftColor complexFeatureShiftColor(&featureShiftColor,&noFeatureShiftColor);
    FindComplexFeature(bitmap, x, y, x1, y1, &firstShiftColor, &complexFeatureShiftColor, sim, order, out);
}
bool CmpColor(Bitmap *bitmap, int x, int y,const char *first,double sim){
    MulShiftColor mulShiftColor(first, strlen(first));
    unsigned char *color = ComputeCoordColor(bitmap, x, y);
    return mulShiftColor.equal(color, sim);
}
