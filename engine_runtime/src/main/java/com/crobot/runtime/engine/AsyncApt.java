package com.crobot.runtime.engine;

import com.crobot.runtime.engine.apt.AsyncResult;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;
import com.crobot.utils.CLog;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AsyncApt extends ObjApt {
    private Future<AsyncResult> future;

    public AsyncApt(Future<AsyncResult> future) {
        this.future = future;
    }

    @Caller("take")
    public Object take(Number timeout) throws InterruptedException, ExecutionException, TimeoutException {
        AsyncResult asyncResult = this.future.get(timeout.longValue(), TimeUnit.MILLISECONDS);
        if (asyncResult == null) {
            return null;
        }
        if (asyncResult.getErrMsg() != null) {
            throw new ContextException(asyncResult.getErrMsg());
        }
        return asyncResult.getData();
    }

    @Caller("isDone")
    public boolean isDone() {
        return this.future.isDone();
    }

    @Caller("cancel")
    public boolean cancel() {
        return this.future.cancel(true);
    }

}
