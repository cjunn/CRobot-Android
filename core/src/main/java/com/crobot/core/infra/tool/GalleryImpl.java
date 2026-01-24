package com.crobot.core.infra.tool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class GalleryImpl implements Gallery {
    private final WindowManager windowManager;
    private final WindowManager.LayoutParams layoutParams;
    private final Handler main = new Handler(Looper.getMainLooper());
    private Context context;
    private View view;


    public GalleryImpl(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.layoutParams = initLayoutParams();
    }

    private void setViewFullScreen(View view) {
        view.setClickable(false);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    private WindowManager.LayoutParams initLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        layoutParams.format = PixelFormat.RGBA_8888; //窗口透明
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL; //窗口位置
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS // 允许窗口超出屏幕边界（全屏关键）
                | WindowManager.LayoutParams.FLAG_FULLSCREEN; // 隐藏状态栏，强化全屏效果
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        return layoutParams;
    }

    @Override
    public synchronized void show(String filepath) {
        Bitmap bitmap = BitmapFactory.decodeFile(filepath);
        ImageView imageView = new ImageView(this.context);
        imageView.setBackgroundColor(Color.argb(220, 0, 0, 0));
        setViewFullScreen(imageView);
        imageView.setImageBitmap(bitmap);
        main.post(() -> {
            this.windowManager.addView(imageView, this.layoutParams);
            view = imageView;
        });
    }

    @Override
    public synchronized void close() {
        main.post(() -> {
            if (view != null) {
                this.windowManager.removeView(view);
                view = null;
            }
        });
    }
}
