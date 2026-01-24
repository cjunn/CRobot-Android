package com.crobot.core.drive.lib;

import android.boostrap.CBootBind;
import android.boostrap.CBootLink;
import android.graphics.Bitmap;

import java.util.List;

public class YoloV8Bridge {
    private static boolean isInit = false;

    public static void initEnv(String soPath) {
        if (isInit) {
            return;
        }
        synchronized (YoloV8Bridge.class) {
            if (isInit) {
                return;
            }
            CBootLink.addNative(YoloV8Bridge.class);
            //System.loadLibrary("YoloV8");
            System.load(soPath);
            isInit = true;
        }
    }

    @CBootBind(300)
    public static native long newYoloV8(String param, String bin, int type, int target_size, boolean useGpu);

    @CBootBind(301)
    public static native List detect(long yoloV8, Bitmap input);

    @CBootBind(302)
    public static native void close(long yoloV8);
}
