package com.crobot.core.infra.tool;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.crobot.core.resource.My;
import com.crobot.core.util.Latch;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class DialogImpl implements Dialog {
    private final Handler main = new Handler(Looper.getMainLooper());
    private final WindowManager windowManager;
    private Context context;
    private Map<View, Boolean> viewMap = new ConcurrentHashMap<>();

    public DialogImpl(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    private WindowManager.LayoutParams createLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        return params;
    }

    private View createDialogView(String title, String message, boolean showInput, String prefill) {
        View dialogView = LayoutInflater.from(context).inflate(My.layout.dialog, null);
        TextView titleView = dialogView.findViewById(My.id.dialog_title);
        TextView messageView = dialogView.findViewById(My.id.dialog_message);
        EditText inputView = dialogView.findViewById(My.id.dialog_input);
        titleView.setText(title);
        if (message != null && !message.isEmpty()) {
            messageView.setText(message);
            messageView.setVisibility(View.VISIBLE);
        } else {
            messageView.setVisibility(View.GONE);
        }
        if (showInput) {
            inputView.setVisibility(View.VISIBLE);
            inputView.setInputType(InputType.TYPE_CLASS_TEXT);
            if (prefill != null) {
                inputView.setText(prefill);
                inputView.selectAll();
            }
        } else {
            inputView.setVisibility(View.GONE);
        }

        return dialogView;
    }


    @Override
    public void alert(String title, String content, Runnable callback) {
        View dialogView = createDialogView(title, content, false, null);
        View overlay = createOverlay();
        Button positiveBtn = dialogView.findViewById(My.id.dialog_positive);
        Button negativeBtn = dialogView.findViewById(My.id.dialog_negative);
        negativeBtn.setVisibility(View.GONE);
        positiveBtn.setText("确定");
        WindowManager.LayoutParams params = createLayoutParams();
        positiveBtn.setOnClickListener(v -> {
            removeView(dialogView);
            removeView(overlay);
            if (callback != null) {
                callback.run();
            }
        });
        Latch latch = new Latch(1);
        main.post(() -> {
            addView(overlay, createOverlayParams());
            addView(dialogView, params);
            latch.countDown();
        });
        latch.await();
    }

    @Override
    public void confirm(String title, String content, Consumer<Boolean> callback) {
        View dialogView = createDialogView(title, content, false, null);
        View overlay = createOverlay();
        Button positiveBtn = dialogView.findViewById(My.id.dialog_positive);
        Button negativeBtn = dialogView.findViewById(My.id.dialog_negative);
        negativeBtn.setVisibility(View.VISIBLE);
        positiveBtn.setText("确定");
        negativeBtn.setText("取消");
        WindowManager.LayoutParams params = createLayoutParams();
        positiveBtn.setOnClickListener(v -> {
            removeView(dialogView);
            removeView(overlay);
            if (callback != null) {
                callback.accept(true);
            }
        });
        negativeBtn.setOnClickListener(v -> {
            removeView(dialogView);
            removeView(overlay);
            if (callback != null) {
                callback.accept(false);
            }
        });
        Latch latch = new Latch(1);
        main.post(() -> {
            addView(overlay, createOverlayParams());
            addView(dialogView, params);
            latch.countDown();
        });
        latch.await();
    }

    @Override
    public void input(String title, String prefill, Consumer<String> callback) {
        View dialogView = createDialogView(title, null, true, prefill);
        View overlay = createOverlay();
        EditText inputView = dialogView.findViewById(My.id.dialog_input);
        Button positiveBtn = dialogView.findViewById(My.id.dialog_positive);
        Button negativeBtn = dialogView.findViewById(My.id.dialog_negative);
        negativeBtn.setVisibility(View.VISIBLE);
        positiveBtn.setText("确定");
        negativeBtn.setText("取消");
        WindowManager.LayoutParams params = createLayoutParams();
        positiveBtn.setOnClickListener(v -> {
            String text = inputView.getText().toString();
            removeView(dialogView);
            removeView(overlay);
            if (callback != null) {
                callback.accept(text);
            }
        });
        negativeBtn.setOnClickListener(v -> {
            removeView(dialogView);
            removeView(overlay);
            if (callback != null) {
                callback.accept(null);
            }
        });
        overlay.setOnClickListener(v -> {
            removeView(dialogView);
            removeView(overlay);
            if (callback != null) {
                callback.accept(null);
            }
        });
        Latch latch = new Latch(1);
        main.post(() -> {
            addView(overlay, createOverlayParams());
            addView(dialogView, params);
            latch.countDown();
        });
        latch.await();
    }

    private void addView(View view, ViewGroup.LayoutParams params) {
        viewMap.put(view, true);
        windowManager.addView(view, params);
    }

    private void removeView(View view) {
        viewMap.remove(view);
        windowManager.removeView(view);
    }


    @Override
    public void clear() {
        Set<View> views = viewMap.keySet();
        for (View view : views) {
            removeView(view);
        }
    }

    private View createOverlay() {
        View overlay = new View(context);
        overlay.setBackgroundColor(0x80000000); // 半透明黑色
        return overlay;
    }

    private WindowManager.LayoutParams createOverlayParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        return params;
    }

}
