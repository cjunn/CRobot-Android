package com.crobot.core.side;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.crobot.core.resource.My;

import java.util.ArrayList;
import java.util.List;

public class ScreenSetting extends ConstraintLayout {
    private Context context;
    private int padWidth = 0;
    private int padHeight = 0;
    private WindowHolder windowHolder;
    private WindowManager windowManager;
    private WinLayoutParams winLayoutParams;
    private ViewGroup body;
    private List<ScreenSettingEvent> events = new ArrayList<>();

    public ScreenSetting(Context context) {
        super(context);
        this.hide();
        this.context = context;
        this.windowHolder = new WindowHolder(context);
        this.setMinWidth(this.windowHolder.getWidth());
        this.setMinHeight(this.windowHolder.getHeight());
        View view = LayoutInflater.from(this.context).inflate(My.layout.screen_setting, null);
        this.addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        this.body = this.findViewById(My.id.screen_setting_body);
        View btnSave = this.findViewById(My.id.screen_setting_save);
        btnSave.setClickable(true);
        btnSave.setOnClickListener((e) -> this.hide());
        View btnUpdate = this.findViewById(My.id.screen_setting_update);
        btnUpdate.setClickable(true);
        btnUpdate.setOnClickListener((e) -> events.forEach(event -> event.updateEvent()));
        this.setClickable(true);
        this.setOnClickListener((event) -> this.hide());
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.winLayoutParams = new WinLayoutParams();
        this.winLayoutParams.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        windowManager.addView(this, this.winLayoutParams);
    }

    public void addEvent(ScreenSettingEvent event) {
        this.events.add(event);
    }

    private void initPad() {
        this.padWidth = (int) (windowHolder.getWidth() * 0.23);
        this.padHeight = (int) (windowHolder.getHeight() * 0.15);
    }

    public boolean isOrientationPortrait() {
        int orientation = getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public void show() {
        this.initPad();
        if (isOrientationPortrait()) {
            this.setPadding(25, padHeight, 25, padHeight);
        } else {
            this.setPadding(padWidth, 25, padWidth, 25);
        }
        this.requestLayout();
        this.setVisibility(View.VISIBLE);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f);
        alphaAnimator.setDuration(120);
        alphaAnimator.start();
    }


    public void hide() {
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f);
        alphaAnimator.setDuration(120);
        alphaAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ScreenSetting.this.setVisibility(View.GONE);
            }
        });
        alphaAnimator.start();
    }

    public ViewGroup getBody() {
        return this.body;
    }

    public void destroy() {
        this.windowManager.removeView(this);
    }
}
