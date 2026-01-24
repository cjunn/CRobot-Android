package com.crobot.core.util;

import java.util.TimerTask;

public class ReTimer {
    private TimerTask timerTask;
    private java.util.Timer timer = new java.util.Timer();

    public synchronized void clean() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        timer.purge();
    }

    public synchronized void submit(Runnable runnable, long delay) {
        clean();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        };
        timer.schedule(timerTask, delay);
    }
}
