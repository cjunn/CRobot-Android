package com.crobot.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Delayer {
    ExecutorService pool = Executors.newSingleThreadExecutor();
    public void delay(long time) {
        try {
            if (time == 0) {
                return;
            }
            pool.submit(() -> {
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {

                }
            }).get();
        } catch (Exception e) {
        }
    }

    public void stop() {
        pool.shutdownNow();
    }
}
