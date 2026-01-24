package com.crobot.core.infra.tool;

import android.content.Context;

public class ProgressLazy implements Progress {
    private Context context;
    private Progress progress;

    public ProgressLazy(Context context) {
        this.context = context;
    }

    private Progress getProgress() {
        if (this.progress != null) {
            return this.progress;
        }
        synchronized (this) {
            if (this.progress != null) {
                return this.progress;
            }
            this.progress = new ProgressImpl(context);
            return this.progress;
        }
    }

    @Override
    public void setProgress(int progress) {
        getProgress().setProgress(progress);
    }

    @Override
    public void show(String title) {
        getProgress().show(title);
    }

    @Override
    public void close() {
        getProgress().close();
    }
}
