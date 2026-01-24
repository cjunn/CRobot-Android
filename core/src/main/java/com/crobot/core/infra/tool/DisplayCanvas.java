package com.crobot.core.infra.tool;

public interface DisplayCanvas {
    DisplayBitmap newCanvas(int w, int h);

    void removeCanvas(DisplayBitmap canvasBitmap);

    void invalidate();

}
