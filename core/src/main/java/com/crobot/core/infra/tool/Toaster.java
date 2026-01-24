package com.crobot.core.infra.tool;

public interface Toaster {
    void show(String message);

    void show(String message, int xOffset, int yOffset);

    void show(String message, int duration, int xOffset, int yOffset);

    void close();
}
