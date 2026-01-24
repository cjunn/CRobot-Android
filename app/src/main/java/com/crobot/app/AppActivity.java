package com.crobot.app;

import android.os.Bundle;
import android.view.ViewGroup;

import com.crobot.core.BackendService;
import com.crobot.core.FrontActivity;

public class AppActivity extends FrontActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public ViewGroup uiContainer() {
        return null;
    }

    @Override
    public Class<? extends BackendService> backendService() {
        return AppService.class;
    }
}