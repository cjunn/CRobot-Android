package com.crobot.runtime.engine;

import com.crobot.runtime.engine.boot.BootUtil;

import java.lang.reflect.Constructor;

public class CallBack {
    private final long hold;
    private final Context context;

    public CallBack(Context context, long hold) {
        this.hold = hold;
        this.context = context;
    }

    public Result apply(Varargs args) {
        return context.callback(this, args);
    }

    public long getHold() {
        return hold;
    }

    public static Constructor<?> getJniConstructor() {
        return BootUtil.getJniConstructor(CallBack.class);
    }

}
