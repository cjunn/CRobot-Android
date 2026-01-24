package com.crobot.app;

import android.app.Activity;

import com.crobot.core.BackendService;
import com.crobot.engine.clua.LuaContextFactory;
import com.crobot.runtime.engine.ContextFactory;

public class AppService extends BackendService {

    @Override
    protected String appVer() {
        return Constant.APP_VER;
    }

    @Override
    protected Class<? extends Activity> openActivityClz() {
        return AppActivity.class;
    }

    @Override
    public ContextFactory getContextFactory() {
        return new LuaContextFactory();
    }

    @Override
    public void output(String tag, String source, int currentLine, String message) {
    }

    @Override
    protected String channelName() {
        return super.channelName();
    }
}
