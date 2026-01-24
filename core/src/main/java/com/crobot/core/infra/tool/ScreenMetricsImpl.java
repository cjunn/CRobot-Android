package com.crobot.core.infra.tool;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class ScreenMetricsImpl implements ScreenMetrics {
    private WindowManager windowManager;
    private int width;
    private int height;
    private int densityDpi;
    private int orientation = 0;


    public ScreenMetricsImpl(Context context) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        width = metrics.widthPixels;
        height = metrics.heightPixels;
        densityDpi = metrics.densityDpi;
        OrientationReceiver orientationReceiver = new OrientationReceiver((ori) -> orientation = ori);
        context.registerReceiver(orientationReceiver, new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
    }

    @Override
    public int getWidth() {
        return getWidth(orientation);
    }

    @Override
    public int getHeight() {
        return getHeight(orientation);
    }

    @Override
    public int getDensityDpi() {
        return densityDpi;
    }

    @Override
    public int getWidth(int orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return height;
        } else {
            return width;
        }
    }

    @Override
    public int getHeight(int orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return width;
        } else {
            return height;
        }
    }

}
