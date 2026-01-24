package com.crobot.runtime.engine.apt;

import com.crobot.runtime.engine.Context;
import com.crobot.runtime.engine.ContextException;
import com.crobot.runtime.engine.apt.anno.Gc;
import com.crobot.runtime.engine.apt.anno.Invoke;
import com.crobot.utils.CLog;

import java.lang.reflect.Method;

public abstract class BaseApt {
    @Gc()
    public int onGc(Context context) {
        return 0;
    }

    private Object handleInvoke(Object invoke) {
        if (invoke == null) {
            return null;
        }
        if (invoke instanceof Object[]) {
            Object[] items = (Object[]) invoke;
            Object[] retItems = new Object[items.length];
            for (int i = 0; i < items.length; i++) {
                retItems[i] = handleInvoke(items[i]);
            }
            return retItems;
        }
        if (invoke instanceof FuncApt) {
            return AptInfoGen.buildAdapterInfo((FuncApt) invoke);
        }
        if (invoke instanceof ObjApt && this == invoke) {
            return AptInfoGen.buildSelfAdapterInfo((ObjApt) invoke);
        }
        if (invoke instanceof ObjApt) {
            return AptInfoGen.buildAdapterInfo((ObjApt) invoke);
        }
        return invoke;
    }


    @Invoke
    public Object onInvoke(Context context, Method method, Object... args) {
        try {
            return handleInvoke(method.invoke(this, args));
        } catch (Throwable e) {
            throw new ContextException(e);
        }
    }

}
