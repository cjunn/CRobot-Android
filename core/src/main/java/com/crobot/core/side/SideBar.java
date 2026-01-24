package com.crobot.core.side;

import android.view.ViewGroup;

public interface SideBar {
    void addEvent(SideBarEvent event);

    ViewGroup getSettingBody();

    void setPlayStatus();

    void setPauseStatus();

    void destroy();
}
