package com.crobot.core.infra.tool;

public interface ScreenTouch {
    boolean tap(float x, float y, int delay);

    boolean swipe(float x1, float y1, float x2, float y2, int duration);
}
