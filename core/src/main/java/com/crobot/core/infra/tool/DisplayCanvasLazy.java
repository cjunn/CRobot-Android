package com.crobot.core.infra.tool;

import android.content.Context;

public class DisplayCanvasLazy implements DisplayCanvas {
    private Context context;
    private DisplayCanvas displayCanvas;

    public DisplayCanvasLazy(Context context) {
        this.context = context;
    }

    private DisplayCanvas getDisplayCanvas() {
        if (this.displayCanvas != null) {
            return this.displayCanvas;
        }
        synchronized (this) {
            if (this.displayCanvas != null) {
                return this.displayCanvas;
            }
            this.displayCanvas = new DisplayCanvasImpl(context);
            return this.displayCanvas;
        }
    }

    @Override
    public DisplayBitmap newCanvas(int w, int h) {
        return getDisplayCanvas().newCanvas(w, h);
    }

    @Override
    public void removeCanvas(DisplayBitmap displayBitmap) {
        getDisplayCanvas().removeCanvas(displayBitmap);
    }

    @Override
    public void invalidate() {
        getDisplayCanvas().invalidate();
    }

}
