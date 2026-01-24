package com.crobot.core.infra.tool;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;

import com.crobot.core.util.Latch;

import java.util.ArrayList;
import java.util.List;

public class DisplayCanvasImpl implements DisplayCanvas {
    private final Handler main = new Handler(Looper.getMainLooper());
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private CanvasView canvasView;
    private List<DisplayBitmap> canvasList = new ArrayList<>();

    public DisplayCanvasImpl(Context context) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        layoutParams = initLayoutParams();
        canvasView = new CanvasView(context);
        canvasView.setFocusable(false);
        canvasView.setClickable(false);
    }

    public synchronized DisplayBitmap newCanvas(int w, int h) {
        DisplayBitmap DisplayBitmap = new DisplayBitmap(w, h);
        if (canvasList.add(DisplayBitmap) && canvasList.size() == 1) {
            Latch latch = new Latch(1);
            main.post(() -> {
                windowManager.addView(canvasView, layoutParams);
                latch.countDown();
            });
            latch.await();
        }
        return DisplayBitmap;
    }

    public synchronized void removeCanvas(DisplayBitmap DisplayBitmap) {
        if (canvasList.remove(DisplayBitmap) && canvasList.size() == 0) {
            Latch latch = new Latch(1);
            main.post(() -> {
                windowManager.removeView(canvasView);
                latch.countDown();
            });
            latch.await();
        } else {
            canvasView.postInvalidate();
        }
    }

    public synchronized void invalidate() {
        canvasView.postInvalidate();
    }

    private WindowManager.LayoutParams initLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        layoutParams.format = PixelFormat.RGBA_8888; //窗口透明
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE; // 关键：不响应任何点击/触摸
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        return layoutParams;
    }

    public class CanvasView extends View {
        public CanvasView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            synchronized (DisplayCanvasImpl.this) {
                for (DisplayBitmap item : canvasList) {
                    canvas.drawBitmap(item.bitmap, 0, 0, null);
                }
            }
        }
    }


}
