package com.crobot.core.side;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.crobot.core.resource.My;
import com.crobot.core.util.ReTimer;

public class CBtn extends ConstraintLayout implements View.OnTouchListener, View.OnClickListener, OptBarEvent {
    private ReTimer hideBtnTimer = new ReTimer();
    private boolean isHide = false;
    private Button btn;
    private WindowManager windowManager;
    private WinLayoutParams layoutParams;
    private WindowHolder windowHolder;
    private int btnFull;
    private int btnHalf;
    private int btnMargin;
    private boolean isRight = true;
    private boolean isMove = false;
    private boolean isExpand = false;
    private long startMoveTime;
    private int lastMoveX;
    private int lastMoveY;
    private OptBar optBar;

    public CBtn(@NonNull Context context, OptBar optBar) {
        super(context);
        this.optBar = optBar;
        this.windowHolder = new WindowHolder(context);
        this.setClickable(false);
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.layoutParams = new WinLayoutParams();
        this.addView(LayoutInflater.from(context).inflate(My.layout.side_c_btn, null));
        this.windowManager.addView(this, layoutParams);
        this.btnFull = (int) this.getResources().getDimension(My.dimen.side_c_btn_size);
        this.btnMargin = (int) this.getResources().getDimension(My.dimen.side_c_btn_margin);
        this.btnHalf = this.btnFull / 2;
        this.btn = this.findViewById(My.id.side_cbtn);
        this.btn.setOnTouchListener(this);
        this.btn.setOnClickListener(this);
        this.btn.setAnimation(null);
        this.windowHolder.registerReceiver(new OrientationReceiver(() -> this.updateInitLayout()));
        this.updateInitLayout();
        this.optBar.addEvent(this);
    }


    private void updateInitLayout() {
        int x = this.windowHolder.getWidth() - this.btnFull - this.btnMargin;
        int y = this.windowHolder.getHeight() / 2 - 60;
        layoutParams.x = x;
        layoutParams.y = y;
        windowManager.updateViewLayout(this, layoutParams);
    }


    private void updateLayout() {
        windowManager.updateViewLayout(this, layoutParams);
    }

    private void updateLayout(int x) {
        layoutParams.x = x;
        windowManager.updateViewLayout(this, layoutParams);
    }

    /**
     * 显示C按钮
     */
    private void showCBtn() {
        if (!this.isHide) {
            return;
        }
        int finalMoveX = isRight ? this.windowHolder.getWidth() - this.getMeasuredWidth() : this.btnMargin;
        this.moveTo(finalMoveX, 3);
        this.expandBar(finalMoveX, layoutParams.y);
        this.isHide = false;
    }

    private void hideCBtn() {
        if (isHide) {
            return;
        }
        int finalMoveX = isRight ? this.windowHolder.getWidth() - this.btnHalf : -this.btnHalf;
        this.moveTo(finalMoveX, 3);
        this.collapseBar();
        isHide = true;
    }


    private void delayHideCBtn() {
        hideBtnTimer.submit(() -> this.post(() -> hideCBtn()), 3000);
    }

    private boolean isCanMove() {
        return !isExpand && !isHide;
    }


    /**
     * 将悬浮操作栏展开化
     */
    private void expandBar(int x, int y) {
        if (isExpand) {
            return;
        }
        this.optBar.expandBar(x, y, isRight);
        isExpand = true;
    }

    //收缩导览行
    private void collapseBar() {
        if (!isExpand) {
            return;
        }
        this.optBar.collapseBar(layoutParams.x, layoutParams.y, isRight);
        isExpand = false;
    }

    /**
     * 切换悬浮操作栏的展开状态
     */
    private void switchExpandBar() {
        if (!isExpand) {
            expandBar(layoutParams.x, layoutParams.y);
        } else {
            collapseBar();
        }
    }

    @Override
    public void onClick(View v) {
        if (this.isHide) {
            this.showCBtn();
            return;
        }
        this.switchExpandBar();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                hideBtnTimer.clean();
                break;
            case MotionEvent.ACTION_UP:
                delayHideCBtn();
                break;
        }
        if (!this.isCanMove()) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastMoveX = (int) event.getRawX();
                lastMoveY = (int) event.getRawY();
                startMoveTime = System.currentTimeMillis();
                isMove = false;
                updateLayout();
                return false;
            case MotionEvent.ACTION_MOVE:
                int rawX = (int) event.getRawX();
                int rawY = (int) event.getRawY();
                int diffX = rawX - lastMoveX;
                int diffY = rawY - lastMoveY;
                int newX = layoutParams.x + diffX;
                int newY = layoutParams.y + diffY;
                int maxWidth = this.windowHolder.getWidth() - this.btnFull;
                int maxHeight = this.windowHolder.getHeight() - this.btnFull;
                newX = newX <= 0 ? 0 : newX;
                newY = newY <= 0 ? 0 : newY;
                newX = newX >= maxWidth ? maxWidth : newX;
                newY = newY >= maxHeight ? maxHeight : newY;
                layoutParams.x = newX;
                layoutParams.y = newY;

                isMove = (Math.abs(diffX) >= 2 || Math.abs(diffY) >= 2) ? true : isMove;
                lastMoveX = rawX;
                lastMoveY = rawY;
                updateCDirection();
                updateLayout();
                return true;
            case MotionEvent.ACTION_UP:
                long curTime = System.currentTimeMillis();
                boolean timeIsMove = (curTime - startMoveTime > 100);
                boolean moveResult = isMove && timeIsMove;
                if (moveResult) {
                    moveToSide();
                }
                isMove = false;
                return moveResult;
        }
        return false;
    }


    private boolean isGoRight() {
        return (layoutParams.x + this.btnFull / 2 >= this.windowHolder.getWidth() / 2);
    }

    private void updateCDirection() {
        if (isGoRight()) {
            this.isRight = true;
            this.btn.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        } else {
            this.isRight = false;
            this.btn.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }


    /**
     * 移动到屏幕侧边
     */
    private void moveToSide() {
        if (isGoRight()) {
            this.isRight = true;
            moveTo(this.windowHolder.getWidth() - this.btnFull - this.btnMargin, 2);
        } else {
            this.isRight = false;
            moveTo(this.btnMargin, 2);
        }
    }


    private void moveTo(int finalMoveX, float scale) {
        //使用动画移动mView
        int duration = Math.abs(layoutParams.x - finalMoveX) / 2;
        duration = (int) (duration * scale);
        ValueAnimator animator = ValueAnimator.ofInt(layoutParams.x, finalMoveX).setDuration(duration);
        animator.addUpdateListener((ValueAnimator animation) -> {
            if (animation != null) {
                int animatedValue = (int) animation.getAnimatedValue();
                updateLayout(animatedValue);
            }
        });
        animator.start();
    }


    @Override
    public void playEvent() {
        hideCBtn();
        btn.setAlpha(0.6f);
        btn.setBackgroundResource(My.drawable.float_bar_c_layer_active);
    }

    @Override
    public void pauseEvent() {
        delayHideCBtn();
        btn.setAlpha(1f);
        btn.setBackgroundResource(My.drawable.float_bar_c_layer);
    }

    @Override
    public void settingEvent() {
        delayHideCBtn();
    }

    public void setActivityStatus() {
        hideCBtn();
        btn.setAlpha(0.6f);
        btn.setBackgroundResource(My.drawable.float_bar_c_layer_active);
    }

    public void setResetStatus() {
        btn.setAlpha(1f);
        btn.setBackgroundResource(My.drawable.float_bar_c_layer);
    }

    public void destroy() {
        this.windowManager.removeView(this);
    }
}
