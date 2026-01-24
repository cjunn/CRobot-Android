package com.crobot.core.infra.tool;

import android.content.Context;
import android.content.Intent;
import android.media.Image;

import java.util.function.Supplier;

public class ScreenCaptureLazy implements ScreenCapture {
    private Context context;
    private int orientation;
    private ScreenCapture screenCapture;
    private ScreenMetrics screenMetrics;
    private Supplier<Intent> intent;

    public ScreenCaptureLazy(Context context, Supplier<Intent> intent, int orientation, ScreenMetrics screenMetrics) {
        this.context = context;
        this.intent = intent;
        this.orientation = orientation;
        this.screenMetrics = screenMetrics;
    }

    private ScreenCapture getScreenCapture() {
        if (this.screenCapture != null) {
            return this.screenCapture;
        }
        synchronized (this) {
            if (this.screenCapture != null) {
                return this.screenCapture;
            }
            this.screenCapture = new ScreenCaptureImpl(context, intent.get(), orientation, screenMetrics);
            return this.screenCapture;
        }
    }

    @Override
    public Image capture() {
        return getScreenCapture().capture();
    }
}
