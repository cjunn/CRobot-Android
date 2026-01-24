package com.crobot.core.drive.lib;

import android.graphics.Bitmap;

import com.crobot.core.drive.BitmapApt;
import com.crobot.runtime.engine.Context;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.JsonBean;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;

import java.util.List;

public class YoloV8Initiator implements Initiator {
    @Override
    public void execute(ContextProxy context) {
        context.setObjApt("YoloV8DetCoco", new YoloV8DetCoco());
        context.setObjApt("YoloV8DetOiv7", new YoloV8DetOiv7());
        context.setObjApt("YoloV8Seg", new YoloV8Seg());
        context.setObjApt("YoloV8Pose", new YoloV8Pose());
        context.setObjApt("YoloV8Cls", new YoloV8Cls());
        context.setObjApt("YoloV8Obb", new YoloV8Obb());
    }

    private static class BaseYoloV8 extends ObjApt {
        protected YoloV8 yoloV8 = new YoloV8();

        public void init(String soPath, String param, String bin, int type, int target_size, Boolean useGpu) {
            yoloV8.init(soPath, param, bin, type, target_size, useGpu);
        }

        @Caller("detect")
        public JsonBean detect(ObjApt input) {
            if (input instanceof BitmapApt) {
                Bitmap bitmap = ((BitmapApt) input).getBitmap();
                List detect = yoloV8.detect(bitmap);
                return JsonBean.create(detect);
            }
            return null;
        }


        @Override
        public int onGc(Context context) {
            yoloV8.close();
            return super.onGc(context);
        }
    }

    private static class YoloV8DetCoco extends BaseYoloV8 {
        @Caller("init")
        public void init(String soPath, String param, String bin, Number target_size, Boolean useGpu) {
            yoloV8.init(soPath, param, bin, 0, target_size.intValue(), useGpu);
        }
    }

    private static class YoloV8DetOiv7 extends BaseYoloV8 {
        @Caller("init")
        public void init(String soPath, String param, String bin, Number target_size, Boolean useGpu) {
            yoloV8.init(soPath, param, bin, 1, target_size.intValue(), useGpu);
        }
    }

    private static class YoloV8Seg extends BaseYoloV8 {
        @Caller("init")
        public void init(String soPath, String param, String bin, Number target_size, Boolean useGpu) {
            yoloV8.init(soPath, param, bin, 2, target_size.intValue(), useGpu);
        }
    }

    private static class YoloV8Pose extends BaseYoloV8 {
        @Caller("init")
        public void init(String soPath, String param, String bin, Number target_size, Boolean useGpu) {
            yoloV8.init(soPath, param, bin, 3, target_size.intValue(), useGpu);
        }
    }

    private static class YoloV8Cls extends BaseYoloV8 {
        @Caller("init")
        public void init(String soPath, String param, String bin, Number target_size, Boolean useGpu) {
            yoloV8.init(soPath, param, bin, 4, target_size.intValue(), useGpu);
        }
    }

    private static class YoloV8Obb extends BaseYoloV8 {
        @Caller("init")
        public void init(String soPath, String param, String bin, Number target_size, Boolean useGpu) {
            yoloV8.init(soPath, param, bin, 5, target_size.intValue(), useGpu);
        }
    }


}
