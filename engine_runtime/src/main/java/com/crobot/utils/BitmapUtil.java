package com.crobot.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class BitmapUtil {

    public static byte[] bitmap2Byte(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        return bos.toByteArray();
    }

    public static Bitmap convertImageToBitmap(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane Y_PLANE = image.getPlanes()[0];
        ByteBuffer buffer = Y_PLANE.getBuffer();
        // 创建一个Bitmap对象
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        return bitmap;
    }

    public static void saveBitmap(String bitName, Bitmap mBitmap) {
        File f = new File("/mnt/shared/Pictures/" + bitName + ".png");
        try {
            f.createNewFile();
        } catch (IOException e) {
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap rect(Bitmap bm, int sx1, int sy1, int sx2, int sy2) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        sx1 = sx1 < 0 ? 0 : sx1;
        sx1 = sx1 > width ? width - 1 : sx1;
        sx2 = sx2 < 0 ? 0 : sx2;
        sx2 = sx2 > width ? width - 1 : sx2;
        sy1 = sy1 < 0 ? 0 : sy1;
        sy1 = sy1 > height ? height - 1 : sy1;
        sy2 = sy2 < 0 ? 0 : sy2;
        sy2 = sy2 > height ? height - 1 : sy2;
        if (sx1 > sx2) {
            int tx = sx1;
            sx1 = sx2;
            sx2 = tx;
        }
        if (sy1 > sy2) {
            int ty = sy1;
            sy1 = sy2;
            sy2 = ty;
        }
        int dw = sx2 - sx1;
        int dh = sy2 - sy1;
        return Bitmap.createBitmap(bm, sx1, sy1, dw, dh);
    }

    public static Bitmap rotation(Bitmap bm, int degree) {
        if (degree == 0) {
            return bm;
        }
        Matrix m = new Matrix();
        m.setRotate(degree);
        Bitmap bm1 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
        bm.recycle();
        return bm1;
    }

    public static Bitmap scale(Bitmap bm, int scale) {
        if (scale == 0) {
            return bm;
        }
        Matrix m = new Matrix();
        m.postScale(scale, scale);
        Bitmap bm1 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
        bm.recycle();
        return bm1;
    }


}
