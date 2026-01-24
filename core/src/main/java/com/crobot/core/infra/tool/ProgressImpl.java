package com.crobot.core.infra.tool;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crobot.core.resource.My;

public class ProgressImpl implements Progress {
    private final WindowManager windowManager;
    private final WindowManager.LayoutParams layoutParams;
    private final View view;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView title;
    private ProgressBar progressBar;
    private OrientationReceiver orientationReceiver;
    private boolean isVisible;


    public ProgressImpl(Context context) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        view = LayoutInflater.from(context).inflate(My.layout.progress_toast, null);
        this.title = view.findViewById(My.id.progress_title);
        this.progressBar = view.findViewById(My.id.progress_bar);
        view.setVisibility(View.GONE);
        layoutParams = initLayoutParams();
        orientationReceiver = new OrientationReceiver((ori) -> view.requestLayout());
        context.registerReceiver(orientationReceiver, new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
    }


    private WindowManager.LayoutParams initLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        layoutParams.format = PixelFormat.RGBA_8888; //窗口透明
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM; //窗口位置
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.y = 45;
        return layoutParams;
    }

    @Override
    public void show(String titleStr) {
        handler.post(() -> {
            if (!isVisible) {
                windowManager.addView(view, layoutParams);
                isVisible = true;
            }
            progressBar.setProgress(0);
            title.setText(titleStr);
            view.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void setProgress(int progress) {
        handler.post(() -> {
            if (!isVisible) {
                windowManager.addView(view, layoutParams);
                isVisible = true;
            }
            progressBar.setProgress(progress);
        });
    }

    @Override
    public void close() {
        handler.post(() -> {
            if (isVisible) {
                windowManager.removeView(view);
            }
            isVisible = false;
        });
    }
}
