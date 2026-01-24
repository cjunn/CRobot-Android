package com.crobot.core.drive.lib;

import static java.lang.Math.max;

import android.graphics.Bitmap;

import com.crobot.core.drive.BitmapApt;
import com.crobot.runtime.engine.Context;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.JsonBean;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;

import java.util.Map;

public class OcrLiteInitiator implements Initiator {
    @Override
    public void execute(ContextProxy context) {
        context.setObjApt("OcrLite", new OcrLiteApt());
    }


    public static class OcrLiteApt extends ObjApt {
        private OcrLite ocrLite = new OcrLite();

        @Caller("init")
        public void init(String soPath,
                         String angleParam, String angleBin,
                         String dbParam, String dbBin,
                         String crnnParam, String crnnBin,
                         String keys, Number numOfThread, Boolean useGpu) {
            ocrLite.init(soPath, angleParam, angleBin, dbParam, dbBin, crnnParam, crnnBin, keys, numOfThread.intValue(), useGpu);
        }

        @Caller("detect")
        public JsonBean detect(ObjApt input,
                               Number padding, Number boxScoreThresh, Number boxThresh,
                               Number unClipRatio, boolean doAngle, boolean mostAngle) {
            if (input instanceof BitmapApt) {
                Bitmap bitmap = ((BitmapApt) input).getBitmap();
                int maxSideLen = max(bitmap.getWidth(), bitmap.getHeight());
                Map detect = ocrLite.detect(bitmap, padding.intValue(), maxSideLen, boxScoreThresh.floatValue(), boxThresh.floatValue(), unClipRatio.floatValue(), doAngle, mostAngle);
                return JsonBean.create(detect);
            }
            return null;
        }

        @Override
        public int onGc(Context context) {
            ocrLite.close();
            return super.onGc(context);
        }
    }

}
