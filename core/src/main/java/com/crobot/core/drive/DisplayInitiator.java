package com.crobot.core.drive;

import android.media.Image;

import com.crobot.core.infra.tool.ScreenCapture;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;
import com.crobot.utils.BitmapUtil;

import java.nio.ByteBuffer;

public class DisplayInitiator implements Initiator {
    private ScreenCapture screenCapture;

    public DisplayInitiator(ScreenCapture screenCapture) {
        this.screenCapture = screenCapture;
    }

    @Override
    public void execute(ContextProxy context) {
        context.setObjApt("Display", new DisplayApt(screenCapture, context));
    }


    public static class DisplayApt extends ObjApt {
        private ScreenCapture screenCapture;
        private ContextProxy context;
        private boolean isKeepDisplay = false;
        private boolean isGetImage = false;

        private Image lstCapture;

        public DisplayApt(ScreenCapture screenCapture, ContextProxy context) {
            this.screenCapture = screenCapture;
            this.context = context;
        }

        @Caller("update")
        public void update() {
            synchronized (DisplayInitiator.class) {
                Image capture = screenCapture.capture();
                Image.Plane plane = capture.getPlanes()[0];
                int width = capture.getWidth();
                int height = capture.getHeight();
                int rowStride = plane.getRowStride();
                int pixelStride = plane.getPixelStride();
                ByteBuffer buffer = plane.getBuffer();
                context.setBitmap(width, height, rowStride, pixelStride, buffer);
                isGetImage = true;
                lstCapture = capture;
            }
        }

        @Caller("updateIfNeed")
        public void updateIfNeed() {
            if (isGetImage && isKeepDisplay) {
                return;
            }
            update();
        }

        @Caller("keepCapture")
        public void keepCapture() {
            update();
            isKeepDisplay = true;
        }

        @Caller("releaseCapture")
        public void releaseCapture() {
            isKeepDisplay = false;
        }

        @Caller("capture")
        public BitmapApt capture() {
            updateIfNeed();
            return new BitmapApt(BitmapUtil.convertImageToBitmap(lstCapture));
        }

    }

}
