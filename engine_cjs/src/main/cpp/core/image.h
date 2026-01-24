//
// Created by 86152 on 2025/12/18.
//

#ifndef CROBOT_IMAGE_H
#define CROBOT_IMAGE_H

struct Bitmap {
    unsigned char *origin;
    int width;
    int height;
    //行宽度
    int rowShift;
    //步进宽
    int pixelStride;
};
struct Point {
    int x;
    int y;

    Point() : x(-1), y(-1) {

    }

    Point(int x, int y) : x(x), y(y) {

    }
};
struct Pixel {
    unsigned char r;
    unsigned char g;
    unsigned char b;
    unsigned char o;
};
struct Color {
    int hex;
};
enum READ_ORDER {
    UP_DOWN_LEFT_RIGHT,
    UP_DOWN_RIGHT_LEFT,
    DOWN_UP_LEFT_RIGHT,
    DOWN_UP_RIGHT_LEFT,
    LEFT_RIGHT_UP_DOWN,
    LEFT_RIGHT_DOWN_UP,
    RIGHT_LEFT_UP_DOWN,
    RIGHT_LEFT_DOWN_UP,
};

void GetSimpleColor(Bitmap *bitmap, int x, int y, Color *out);
bool FindColor(Bitmap *bitmap, int x, int y, int x1, int y1,const char *mulShift, double sim, int order, Point *out);
void FindFeature(Bitmap *bitmap, int x, int y, int x1, int y1,const char *first,const char *feature, double sim, int order, Point *out);
void FindComplexFeature(Bitmap *bitmap, int x, int y, int x1, int y1, const char *first, const char *featureString, const char *noFeatureString, double sim, double tolerance, int order, Point *out);
bool CmpColor(Bitmap *bitmap, int x, int y,const char *first,double sim);
#endif //CROBOT_IMAGE_H
