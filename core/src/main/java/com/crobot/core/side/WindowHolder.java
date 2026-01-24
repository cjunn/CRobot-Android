package com.crobot.core.side;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.WindowManager;

public class WindowHolder {
    private Context context;
    private OrientationReceiver orientationReceiver;
    private WindowManager windowManager;

    public WindowHolder(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public void registerReceiver(OrientationReceiver orientationReceiver) {
        this.orientationReceiver = orientationReceiver;
        context.registerReceiver(orientationReceiver, new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
    }

    public void unregisterReceiver() {
        if (this.orientationReceiver != null) {
            context.unregisterReceiver(this.orientationReceiver);
            this.orientationReceiver = null;
        }
    }

    public int getWidth() {
        return this.windowManager.getDefaultDisplay().getWidth();
    }

    public int getHeight() {
        return this.windowManager.getDefaultDisplay().getHeight();
    }


}
