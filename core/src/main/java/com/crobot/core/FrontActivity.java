package com.crobot.core;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.crobot.core.side.SideBar;
import com.crobot.core.side.SideBarImpl;
import com.crobot.core.ui.core.UIContext;
import com.crobot.core.ui.core.UIContextImpl;

public abstract class FrontActivity extends AppCompatActivity {
    protected UIContext uiContext;

    protected SideBar sideBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.uiContext = new UIContextImpl(this);
        this.sideBar = new SideBarImpl(this);
        this.bindService(this.uiContext, this.sideBar);
        this.uiContext.bindViewGroup(() -> this.uiContainer());
    }

    private void bindService(UIContext uiContext, SideBar sideBar) {
        Intent intent = this.getIntent();
        intent.setClass(this, this.backendService());
        this.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                BackendService.BackendBinder binder = (BackendService.BackendBinder) service;
                binder.initEnv(uiContext, sideBar);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);
    }

    public abstract ViewGroup uiContainer();

    public abstract Class<? extends BackendService> backendService();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.sideBar.destroy();
    }
}
