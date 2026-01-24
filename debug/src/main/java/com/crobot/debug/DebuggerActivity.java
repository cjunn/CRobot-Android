package com.crobot.debug;

import android.os.Bundle;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

import com.crobot.core.BackendService;
import com.crobot.core.FrontActivity;

public class DebuggerActivity extends FrontActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_debugger);
        this.startFlick();
    }

    @Override
    public ViewGroup uiContainer() {
        return this.findViewById(R.id.debugger_frame_body);
    }

    @Override
    public Class<? extends BackendService> backendService() {
        return DebuggerService.class;
    }


    public void startFlick() {
        Animation alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setDuration(800);
        alphaAnimation.setInterpolator(new LinearInterpolator());
        alphaAnimation.setRepeatCount(Animation.INFINITE);
        alphaAnimation.setRepeatMode(Animation.REVERSE);
        this.findViewById(R.id.debugger_lamp).startAnimation(alphaAnimation);
    }

}