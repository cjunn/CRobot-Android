package com.crobot.core.drive.lib;

import android.boostrap.CBootBind;
import android.boostrap.CBootLink;
import android.graphics.Bitmap;

import java.util.Map;

public class OrcLiteBridge {
    private static boolean isInit = false;

    public static void initEnv(String soPath) {
        if (isInit) {
            return;
        }
        synchronized (OrcLiteBridge.class) {
            if (isInit) {
                return;
            }
            CBootLink.addNative(OrcLiteBridge.class);
            //System.loadLibrary("OcrLite");
            System.load(soPath);
            isInit = true;
        }
    }

    @CBootBind(200)
    public static native long newOcrLite(String angleParam, String angleBin,
                                         String dbParam, String dbBin,
                                         String crnnParam, String crnnBin,
                                         String keys, int numOfThread, boolean useGpu);

    @CBootBind(201)
    public static native Map detect(long ocrLite, Bitmap input,
                                    int padding, int maxSideLen,
                                    float boxScoreThresh, float boxThresh,
                                    float unClipRatio, boolean doAngle, boolean mostAngle);

    @CBootBind(202)
    public static native void close(long ocrLite);

}
