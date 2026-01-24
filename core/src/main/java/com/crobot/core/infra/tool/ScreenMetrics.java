package com.crobot.core.infra.tool;

public interface ScreenMetrics {
    int getWidth();

    int getHeight();

    int getDensityDpi();

    int getWidth(int orientation);

    int getHeight(int orientation);
}
