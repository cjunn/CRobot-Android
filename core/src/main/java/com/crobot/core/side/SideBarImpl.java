package com.crobot.core.side;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;

import com.crobot.core.util.Latch;

import java.util.ArrayList;
import java.util.List;

public class SideBarImpl implements OptBarEvent, ScreenSettingEvent, SideBar {
    private final Handler main = new Handler(Looper.getMainLooper());
    private ScreenSetting screenSetting;
    private CBtn cBtn;
    private OptBar optBar;
    private List<SideBarEvent> events = new ArrayList<>();

    public SideBarImpl(Context context) {
        this.optBar = new OptBar(context);
        this.cBtn = new CBtn(context, this.optBar);
        this.screenSetting = new ScreenSetting(context);
        this.optBar.addEvent(this);
        this.screenSetting.addEvent(this);
    }

    @Override
    public void addEvent(SideBarEvent event) {
        events.add(event);
    }

    @Override
    public ViewGroup getSettingBody() {
        return this.screenSetting.getBody();
    }

    private void _setPlayStatus() {
        optBar.setPlayStatus();
        cBtn.setResetStatus();
    }

    private void _setPauseStatus() {
        optBar.setPauseStatus();
        cBtn.setActivityStatus();
    }


    @Override
    public void setPlayStatus() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            _setPlayStatus();
            return;
        }
        Latch latch = new Latch(1);
        main.post(() -> {
            _setPlayStatus();
            latch.countDown();
        });
        latch.await();
    }

    @Override
    public void setPauseStatus() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            _setPauseStatus();
            return;
        }
        Latch latch = new Latch(1);
        main.post(() -> {
            _setPauseStatus();
            latch.countDown();
        });
        latch.await();
    }


    @Override
    public void playEvent() {
        events.forEach(event -> event.barPlayEvent());
    }

    @Override
    public void pauseEvent() {
        events.forEach(event -> event.barPauseEvent());
    }

    @Override
    public void settingEvent() {
        this.screenSetting.show();
    }

    @Override
    public void updateEvent() {
        events.forEach(event -> event.barUpdateEvent());
    }

    @Override
    public void destroy() {
        this.screenSetting.destroy();
        this.cBtn.destroy();
        this.optBar.destroy();
    }

}
