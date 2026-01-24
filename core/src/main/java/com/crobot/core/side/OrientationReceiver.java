package com.crobot.core.side;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Display;
import android.view.WindowManager;

public class OrientationReceiver extends BroadcastReceiver {
    private int lstOrientation = -1;
    private Runnable runnable;

    public OrientationReceiver(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_CONFIGURATION_CHANGED.equals(intent.getAction())) {
            return;
        }
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int currentOrientation = display.getRotation();
        if (currentOrientation == lstOrientation) {
            return;
        }
        lstOrientation = currentOrientation;
        runnable.run();
    }
}