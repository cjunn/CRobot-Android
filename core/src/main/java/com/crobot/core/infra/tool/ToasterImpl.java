package com.crobot.core.infra.tool;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.crobot.core.resource.My;

import java.util.Timer;
import java.util.TimerTask;

public class ToasterImpl implements Toaster {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final WindowManager windowManager;
    private final WindowManager.LayoutParams layoutParams;
    private final View view;
    private final TextView textView;
    private Timer timer = new Timer();
    private TimerTask lastTimerTask;
    private boolean isShow = false;
    private boolean isVisibility = false;
    public ToasterImpl(Context context) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        view = LayoutInflater.from(context).inflate(My.layout.toaster, null);
        textView = view.findViewById(My.id.toaster_text);
        view.setVisibility(View.GONE);
        layoutParams = initLayoutParams();
    }

    private WindowManager.LayoutParams initLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        layoutParams.format = PixelFormat.RGBA_8888; //窗口透明
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM; //窗口位置
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        return layoutParams;
    }

    public void show(String message) {
        show(message, 3500, 0, 25);
    }

    public void show(String message, int xOffset, int yOffset) {
        show(message, 2000, xOffset, yOffset);
    }

    public void show(String message, int duration, int xOffset, int yOffset) {
        handler.post(() -> {
            if (!isVisibility) {
                isVisibility = true;
                windowManager.addView(view, layoutParams);
            }
            _show(message, xOffset, yOffset);
            _timerClose(duration);
        });
    }

    private void _timerClose(int duration) {
        duration = duration == 0 ? Integer.MAX_VALUE : duration;
        lastTimerTask = new TimerTask() {
            @Override
            public void run() {
                close();
            }
        };
        timer.schedule(lastTimerTask, duration);
    }

    private synchronized void _show(String message, Integer xOffset, Integer yOffset) {
        _close(false);
        if (!(layoutParams.x == xOffset && layoutParams.y == yOffset)) {
            layoutParams.x = xOffset;
            layoutParams.y = yOffset;
            windowManager.updateViewLayout(view, layoutParams);
        }
        textView.setText(message);
        if (!isShow) {
            view.setVisibility(View.VISIBLE);
            view.invalidate();
        }
        isShow = true;
    }

    public void close() {
        handler.post(() -> _close(true));
    }

    public synchronized void _close(boolean update) {
        if (lastTimerTask != null) {
            lastTimerTask.cancel();
            timer.purge();
            lastTimerTask = null;
        }
        if (update) {
            view.setVisibility(View.GONE);
            view.invalidate();
            isShow = false;
        }
    }


}
