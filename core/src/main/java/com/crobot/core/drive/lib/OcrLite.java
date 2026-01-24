package com.crobot.core.drive.lib;

import android.graphics.Bitmap;

import com.crobot.core.util.FileUtil;
import com.crobot.runtime.engine.ContextException;

import java.util.Map;

public class OcrLite {
    private long ocrLitePtr;


    public void init(String soPath,
                     String angleParam, String angleBin,
                     String dbParam, String dbBin,
                     String crnnParam, String crnnBin,
                     String keys, int numOfThread, boolean useGpu) {
        FileUtil.checkFile(soPath, "soPath");
        FileUtil.checkFile(angleParam, "angleParam");
        FileUtil.checkFile(angleBin, "angleBin");
        FileUtil.checkFile(dbParam, "dbParam");
        FileUtil.checkFile(dbBin, "dbBin");
        FileUtil.checkFile(dbBin, "keys");

        OrcLiteBridge.initEnv(soPath);
        ocrLitePtr = OrcLiteBridge.newOcrLite(angleParam, angleBin, dbParam, dbBin, crnnParam, crnnBin, keys, numOfThread, useGpu);
    }

    public Map detect(Bitmap input,
                      int padding, int maxSideLen,
                      float boxScoreThresh, float boxThresh,
                      float unClipRatio, boolean doAngle, boolean mostAngle) {
        if (ocrLitePtr == 0) {
            throw new ContextException("OcrLite has not been initialized yet!");
        }
        return OrcLiteBridge.detect(ocrLitePtr, input, padding, maxSideLen, boxScoreThresh, boxThresh, unClipRatio, doAngle, mostAngle);
    }

    public void close() {
        if (ocrLitePtr == 0) {
            return;
        }
        OrcLiteBridge.close(ocrLitePtr);
    }

}
