package com.crobot.runtime.engine;

import androidx.annotation.Nullable;

import com.crobot.utils.CLog;

import java.lang.ref.WeakReference;

public class EngineImp implements Engine {
    private ContextFactory contextFactory;
    private WeakReference<Context> mainContext;
    private Object doingLock = new Object();
    private boolean closed = false;
    private byte[] zip;
    private RuntimeEvent runtimeEvent = new RuntimeSimpleEvent();

    public EngineImp(ContextFactory contextFactory, RuntimeEvent runtimeEvent, byte[] zip) {
        if (contextFactory == null) {
            throw new ContextException("contextFactory不可为空");
        }
        this.runtimeEvent = runtimeEvent;
        this.zip = zip;
        this.contextFactory = contextFactory;
    }

    public EngineImp(ContextFactory contextFactory) {
        if (contextFactory == null) {
            throw new ContextException("contextFactory不可为空");
        }
        this.zip = new byte[0];
        this.contextFactory = contextFactory;
    }

    @Nullable
    private ContextProxy getContextProxy() {
        ContextProxy mContext;
        synchronized (doingLock) {
            if (closed) {
                return null;
            }
            mContext = contextFactory.newContext();
            this.mainContext = new WeakReference<>(mContext);
        }
        mContext.init(zip);
        return mContext;
    }


    private Result start(java.util.function.Function<ContextProxy, Result> runnable) {
        try {
            runtimeEvent.startEvent();
            ContextProxy mContext = getContextProxy();
            if (mContext == null) {
                return null;
            }
            Result apply = runnable.apply(mContext);
            runtimeEvent.successEvent();
            return apply;
        } catch (Exception e) {
            runtimeEvent.errorEvent(e);
        } finally {
            this.close();
            runtimeEvent.stopEvent();
        }
        return new Result(null);
    }

    @Override
    public Result start(String module, String func) {
        return start((mContext) -> mContext.start(module, func));
    }

    @Override
    public Result start(String cmdline, Varargs args) {
        return start((mContext) -> mContext.start(cmdline, args));
    }

    @Override
    public Result start(Function function, Varargs args) {
        return start((mContext) -> mContext.start(function, args));
    }


    @Override
    public void close() {
        if (this.mainContext == null) {
            return;
        }
        Context mainContext = this.mainContext.get();
        if (mainContext == null) {
            return;
        }
        synchronized (doingLock) {
            if (closed) {
                return;
            }
            mainContext.close();
            this.closed = true;
        }
    }

}
