package com.crobot.core.util;

import java.util.concurrent.CountDownLatch;

public class Latch {
    private final CountDownLatch latch;

    public Latch(int count) {
        this.latch = new CountDownLatch(count);
    }

    public void countDown() {
        this.latch.countDown();
    }

    public void await() {
        try {
            this.latch.await();
        } catch (InterruptedException e) {
        }
    }


}
