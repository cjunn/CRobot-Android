package com.crobot.core.infra.tool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Display;
import android.view.WindowManager;

import java.util.function.Consumer;

public class OrientationReceiver extends BroadcastReceiver {
    private int lstOrientation = -1;
    private Consumer<Integer> runnable;

    public OrientationReceiver(Consumer<Integer> runnable) {
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
        runnable.accept(currentOrientation);
    }
}