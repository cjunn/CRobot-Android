package com.crobot.core.side;

import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.WindowManager;

public class WinLayoutParams extends WindowManager.LayoutParams {
    public WinLayoutParams() {
        this.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        this.format = PixelFormat.RGBA_8888; //窗口透明
        this.gravity = Gravity.LEFT | Gravity.TOP; //窗口位置
        this.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        this.width = WindowManager.LayoutParams.WRAP_CONTENT;
        this.height = WindowManager.LayoutParams.WRAP_CONTENT;
    }
}
