package com.crobot.core.infra.tool;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class DeviceImpl implements Device {
    private WindowManager windowManager;

    public DeviceImpl(Context context) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public int getWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    @Override
    public int getHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

}
