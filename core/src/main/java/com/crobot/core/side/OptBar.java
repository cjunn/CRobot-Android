package com.crobot.core.side;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.crobot.core.resource.My;

import java.util.ArrayList;
import java.util.List;

public class OptBar extends ConstraintLayout {
    private WindowManager windowManager;
    private WinLayoutParams layoutParams;
    private int selfWidth;
    private int btnMargin;
    private int btnPadding;
    private int btnCFull;
    private WindowHolder windowHolder;
    private boolean isPlay = false;
    private List<OptBarEvent> events = new ArrayList<>();
    private Button playPauseBtn;
    private Button settingBtn;

    public OptBar(@NonNull Context context) {
        super(context);
        this.layoutParams = new WinLayoutParams();
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.addView(LayoutInflater.from(context).inflate(My.layout.side_opt_bar, null));
        this.windowManager.addView(this, layoutParams);
        this.btnCFull = (int) this.getResources().getDimension(My.dimen.side_c_btn_size);
        this.btnPadding = this.btnCFull - (int) this.getResources().getDimension(My.dimen.side_btn_circle_1);
        this.btnMargin = (int) this.getResources().getDimension(My.dimen.side_btn_margin);
        this.windowHolder = new WindowHolder(context);
        this.playPauseBtn = findViewById(My.id.side_play_pause_btn);
        this.settingBtn = findViewById(My.id.side_setting_btn);
        this.initPlayPauseBtn();
        this.initSettingBtn();
        this.initSelfWidth();
    }

    public void addEvent(OptBarEvent event) {
        events.add(event);
    }

    private void initSelfWidth() {
        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        this.measure(width, height);
        this.selfWidth = this.getMeasuredWidth();
        this.setVisibility(GONE);
    }

    private void initPlayPauseBtn() {
        playPauseBtn.setBackgroundResource(My.drawable.float_bar_play_layer);
        playPauseBtn.setOnClickListener(v -> {
            isPlay = !isPlay;
            setIsPlay(isPlay);
        });
    }

    private void initSettingBtn() {
        settingBtn.setOnClickListener((v) -> events.forEach(event -> event.settingEvent()));
    }

    private void setIsPlay(boolean isPlay) {
        if (isPlay) {
            playPauseBtn.setBackgroundResource(My.drawable.float_bar_pause_layer);
            events.forEach(event -> event.playEvent());
        } else {
            playPauseBtn.setBackgroundResource(My.drawable.float_bar_play_layer);
            events.forEach(event -> event.pauseEvent());
        }
        playPauseBtn.invalidate();
    }

    private void setIsPlayStatus(boolean isPlay) {
        if (isPlay) {
            playPauseBtn.setBackgroundResource(My.drawable.float_bar_pause_layer);
        } else {
            playPauseBtn.setBackgroundResource(My.drawable.float_bar_play_layer);
        }
        playPauseBtn.invalidate();
    }


    public void expandBar(int x, int y, boolean isRight) {
        if (isRight) {
            this.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        } else {
            this.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        int startX = isRight ? (this.windowHolder.getWidth() + 5) : (-selfWidth - 5);
        int endX = isRight ? (x - selfWidth - this.btnMargin) : (x + this.btnCFull + this.btnMargin);
        updateLayout(startX, y + btnPadding);
        moveTo(endX, 1.3f, null);
        this.setVisibility(VISIBLE);
        this.requestLayout();
    }

    private void updateLayout(int x, int y) {
        layoutParams.x = x;
        layoutParams.y = y;
        windowManager.updateViewLayout(this, layoutParams);
    }

    private void updateLayout(int x) {
        layoutParams.x = x;
        windowManager.updateViewLayout(this, layoutParams);
    }

    private void moveTo(int finalMoveX, float scale, Runnable runnable) {
        //使用动画移动mView
        int duration = Math.abs(layoutParams.x - finalMoveX) / 2;
        duration = (int) (duration * scale);
        ValueAnimator animator = ValueAnimator.ofInt(layoutParams.x, finalMoveX).setDuration(duration);
        animator.addUpdateListener((ValueAnimator animation) -> {
            if (animation != null) {
                int animatedValue = (int) animation.getAnimatedValue();
                updateLayout(animatedValue);
                if (animatedValue == finalMoveX && runnable != null) {
                    runnable.run();
                }
            }
        });
        animator.start();
    }

    public void collapseBar(int x, int y, boolean isRight) {
        int startX = isRight ? (x - selfWidth - this.btnMargin) : (x + this.btnCFull + this.btnMargin);
        int endX = isRight ? (this.windowHolder.getWidth() + 5) : (-selfWidth - 5);
        updateLayout(startX, y + btnPadding);
        moveTo(endX, 1.3f, null);
        this.requestLayout();
    }

    public void setPlayStatus() {
        if (!this.isPlay) {
            return;
        }
        this.isPlay = false;
        this.setIsPlayStatus(false);
    }

    public void setPauseStatus() {
        if (this.isPlay) {
            return;
        }
        this.isPlay = true;
        this.setIsPlayStatus(true);
    }

    public void destroy() {
        this.windowManager.removeView(this);
    }
}
