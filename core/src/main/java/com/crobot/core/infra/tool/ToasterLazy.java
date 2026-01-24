package com.crobot.core.infra.tool;

import android.content.Context;

public class ToasterLazy implements Toaster {
    private Context context;
    private Toaster toaster;

    public ToasterLazy(Context context) {
        this.context = context;
    }

    private Toaster getToaster() {
        if (this.toaster != null) {
            return this.toaster;
        }
        synchronized (this) {
            if (this.toaster != null) {
                return this.toaster;
            }
            this.toaster = new ToasterImpl(context);
            return this.toaster;
        }
    }


    @Override
    public void show(String message) {
        getToaster().show(message);
    }

    @Override
    public void show(String message, int xOffset, int yOffset) {
        getToaster().show(message, xOffset, yOffset);
    }

    @Override
    public void show(String message, int duration, int xOffset, int yOffset) {
        getToaster().show(message, duration, xOffset, yOffset);
    }

    @Override
    public void close() {
        getToaster().close();
    }
}
