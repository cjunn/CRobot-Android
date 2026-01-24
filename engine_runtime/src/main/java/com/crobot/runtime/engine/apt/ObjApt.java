package com.crobot.runtime.engine.apt;

import com.crobot.runtime.engine.Context;
import com.crobot.runtime.engine.apt.anno.Gc;

public abstract class ObjApt extends BaseApt {
    @Gc()
    public int onGc(Context context) {
        return 0;
    }
}
