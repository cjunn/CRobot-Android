//
// Created by 86152 on 2025/11/5.
//

#ifndef CROBOT_IMAGE_H
#define CROBOT_IMAGE_H


#include <cmath>

namespace Image {
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

    template<class T1>
    static bool
    upDownLeftRightReadColor(Bitmap *bitmap, int x, int y, int x1, int y1, T1 *comparator) {
        const unsigned char *moveVerticalPointer;
        const unsigned char *moveLinePointer =
                bitmap->origin + y * bitmap->rowShift + x * bitmap->pixelStride;
        for (int intx = x; intx <= x1; intx++) {
            moveVerticalPointer = moveLinePointer;
            for (int inty = y; inty <= y1; inty++) {
                if (comparator->compare(intx, inty, moveVerticalPointer))
                    return true;
                moveVerticalPointer += bitmap->rowShift;
            }
            moveLinePointer += bitmap->pixelStride;
        }
        return false;
    }

    template<class T1>
    static bool
    upDownRightLeftReadColor(Bitmap *bitmap, int x, int y, int x1, int y1, T1 *comparator) {
        const unsigned char *moveVerticalPointer;
        const unsigned char *moveLinePointer =
                bitmap->origin + y * bitmap->rowShift + x1 * bitmap->pixelStride;
        for (int intx = x1; intx >= x; intx--) {
            moveVerticalPointer = moveLinePointer;
            for (int inty = y; inty <= y1; inty++) {
                if (comparator->compare(intx, inty, moveVerticalPointer))
                    return true;
                moveVerticalPointer += bitmap->rowShift;
            }
            moveLinePointer -= bitmap->pixelStride;
        }
        return false;
    }

    template<class T1>
    static bool
    downUpLeftRightReadColor(Bitmap *bitmap, int x, int y, int x1, int y1, T1 *comparator) {
        const unsigned char *moveVerticalPointer;
        const unsigned char *moveLinePointer =
                bitmap->origin + y1 * bitmap->rowShift + x * bitmap->pixelStride;
        for (int intx = x; intx <= x1; intx++) {
            moveVerticalPointer = moveLinePointer;
            for (int inty = y1; inty >= y; inty--) {
                if (comparator->compare(intx, inty, moveVerticalPointer))
                    return true;
                moveVerticalPointer -= bitmap->rowShift;
            }
            moveLinePointer += bitmap->pixelStride;
        }
        return false;
    }

    template<class T1>
    static bool
    downUpRightLeftReadColor(Bitmap *bitmap, int x, int y, int x1, int y1, T1 *comparator) {
        const unsigned char *moveVerticalPointer;
        const unsigned char *moveLinePointer =
                bitmap->origin + y1 * bitmap->rowShift + x1 * bitmap->pixelStride;
        for (int intx = x1; intx >= x; intx--) {
            moveVerticalPointer = moveLinePointer;
            for (int inty = y1; inty >= y; inty--) {
                if (comparator->compare(intx, inty, moveVerticalPointer))
                    return true;
                moveVerticalPointer -= bitmap->rowShift;
            }
            moveLinePointer -= bitmap->pixelStride;
        }
        return false;
    }

    template<class T1>
    static bool
    leftRightUpDownReadColor(Bitmap *bitmap, int x, int y, int x1, int y1, T1 *comparator) {
        const unsigned char *moveLinePointer;
        const unsigned char *moveVerticalPointer =
                bitmap->origin + y * bitmap->rowShift + x * bitmap->pixelStride;
        for (int inty = y; inty <= y1; inty++) {
            moveLinePointer = moveVerticalPointer;
            for (int intx = x; intx <= x1; intx++) {
                if (comparator->compare(intx, inty, moveLinePointer))
                    return true;
                moveLinePointer += bitmap->pixelStride;
            }
            moveVerticalPointer += bitmap->rowShift;
        }
        return false;
    }

    template<class T1>
    static bool
    leftRightDownUpReadColor(Bitmap *bitmap, int x, int y, int x1, int y1, T1 *comparator) {
        const unsigned char *moveLinePointer;
        const unsigned char *moveVerticalPointer =
                bitmap->origin + y1 * bitmap->rowShift + x * bitmap->pixelStride;
        for (int inty = y1; inty >= y; inty--) {
            moveLinePointer = moveVerticalPointer;
            for (int intx = x; intx <= x1; intx++) {
                if (comparator->compare(intx, inty, moveLinePointer))
                    return true;
                moveLinePointer += bitmap->pixelStride;
            }
            moveVerticalPointer -= bitmap->rowShift;
        }
        return false;
    }

    template<class T1>
    static bool
    rightLeftUpDownReadColor(Bitmap *bitmap, int x, int y, int x1, int y1, T1 *comparator) {
        const unsigned char *moveLinePointer;
        const unsigned char *moveVerticalPointer =
                bitmap->origin + y * bitmap->rowShift + x1 * bitmap->pixelStride;
        for (int inty = y; inty <= y1; inty++) {
            moveLinePointer = moveVerticalPointer;
            for (int intx = x1; intx >= x; intx--) {
                if (comparator->compare(intx, inty, moveLinePointer))
                    return true;
                moveLinePointer -= bitmap->pixelStride;
            }
            moveVerticalPointer += bitmap->rowShift;
        }
        return false;
    }

    template<class T1>
    static bool
    rightLeftDownUpReadColor(Bitmap *bitmap, int x, int y, int x1, int y1, T1 *comparator) {
        const unsigned char *moveLinePointer;
        const unsigned char *moveVerticalPointer =
                bitmap->origin + y1 * bitmap->rowShift + x1 * bitmap->pixelStride;
        for (int inty = y1; inty >= y; inty--) {
            moveLinePointer = moveVerticalPointer;
            for (int intx = x1; intx >= x; intx--) {
                if (comparator->compare(intx, inty, moveLinePointer))
                    return true;
                moveLinePointer -= bitmap->pixelStride;
            }
            moveVerticalPointer -= bitmap->rowShift;
        }
        return false;
    }

    static int computeCharBit(int c) {
        if (c <= '9')
            c -= '0';
        else if (c <= 'F')
            c -= 55;
        else
            c -= 87;
        return c;
    }

    static void swapPosition(int &p1, int &p2) {
        if (p1 > p2) {
            int tmp;
            p1 = tmp;
            p1 = p2;
            p2 = tmp;
        }
    }

    static void limitedScope(int &p, int &scope) {
        if (p < 0) {
            p = 0;
        } else if (p >= scope) {
            p = scope - 1;
        }
    }

    static char computeIntBit(int c) {
        if (c <= 9)
            return c + '0';
        else
            return c + 55;
    }

    template<class T1>
    bool orderFindColor(Bitmap *bitmap, int x, int y, int x1, int y1, int readOrder, T1 *comparator) {
        switch (readOrder) {
            case UP_DOWN_LEFT_RIGHT:
                return upDownLeftRightReadColor(bitmap, x, y, x1, y1, comparator);
            case UP_DOWN_RIGHT_LEFT:
                return upDownRightLeftReadColor(bitmap, x, y, x1, y1, comparator);
            case DOWN_UP_LEFT_RIGHT:
                return downUpLeftRightReadColor(bitmap, x, y, x1, y1, comparator);
            case DOWN_UP_RIGHT_LEFT:
                return downUpRightLeftReadColor(bitmap, x, y, x1, y1, comparator);
            case LEFT_RIGHT_UP_DOWN:
                return leftRightUpDownReadColor(bitmap, x, y, x1, y1, comparator);
            case RIGHT_LEFT_UP_DOWN:
                return rightLeftUpDownReadColor(bitmap, x, y, x1, y1, comparator);
            case LEFT_RIGHT_DOWN_UP:
                return leftRightDownUpReadColor(bitmap, x, y, x1, y1, comparator);
            case RIGHT_LEFT_DOWN_UP:
                return rightLeftDownUpReadColor(bitmap, x, y, x1, y1, comparator);
        }
        return false;
    }

    int compareColor(const unsigned char *color, const unsigned char *color1,
                     const unsigned char *shift, double sim) {
        int r0 = abs(color1[0] - color[0]);
        int r1 = abs(color1[1] - color[1]);
        int r2 = abs(color1[2] - color[2]);
        int tr = r0 + r1 + r2;
        return ((double) 1 - (double) tr / (double) 765 >= sim) ||
               (r0 <= shift[0] && r1 <= shift[1] && r2 <= shift[2]);
    }

    int toIntColor(const char *s) {
        int r = 0;
        r |= (computeCharBit(s[0]) << 20);
        r |= (computeCharBit(s[1]) << 16);
        r |= (computeCharBit(s[2]) << 12);
        r |= (computeCharBit(s[3]) << 8);
        r |= (computeCharBit(s[4]) << 4);
        r |= (computeCharBit(s[5]) << 0);
        return r;
    }

    bool isInBitmapScope(Bitmap *bitmap, int &x, int &y) {
        limitedScope(x, bitmap->width);
        limitedScope(y, bitmap->height);
        return true;
    }

    bool isInBitmapScope(Bitmap *bitmap, int &x, int &y, int &x1, int &y1) {
        limitedScope(x, bitmap->width);
        limitedScope(x1, bitmap->width);
        limitedScope(y, bitmap->height);
        limitedScope(y1, bitmap->height);
        swapPosition(x, x1);
        swapPosition(y, y1);
        return true;
    }

    unsigned char *computeCoordColor(Bitmap *bitmap, int x, int y) {
        return bitmap->origin + y * bitmap->rowShift + x * bitmap->pixelStride;
    }

    void toCharColor(const int s, char *p) {
        p[0] = computeIntBit((s >> 20) & 15);
        p[1] = computeIntBit((s >> 16) & 15);
        p[2] = computeIntBit((s >> 12) & 15);
        p[3] = computeIntBit((s >> 8) & 15);
        p[4] = computeIntBit((s >> 4) & 15);
        p[5] = computeIntBit((s >> 0) & 15);
        p[6] = '\0';
    }

    int shiftColorDiff(char* color1,char* color){
        int r0 = abs(color1[0] - color[0]);
        int r1 = abs(color1[1] - color[1]);
        int r2 = abs(color1[2] - color[2]);
        return r0 + r1 + r2;
    }
} // Image

#endif //CROBOT_IMAGE_H
