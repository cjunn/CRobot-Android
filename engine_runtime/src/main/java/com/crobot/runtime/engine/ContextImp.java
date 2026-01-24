package com.crobot.runtime.engine;

import com.crobot.runtime.engine.apt.AsyncResult;

import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public abstract class ContextImp implements Context {
    private byte[] zip;
    private ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 30,
            30, TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new NameFactory("Async"));
    private ExecutorCompletionService<AsyncResult> executor = new ExecutorCompletionService(pool);

    @Override
    public void init(byte[] zip) {
        this.zip = zip;
    }

    @Override
    public <T> AsyncApt submitTask(Supplier<T> callable) {
        return new AsyncApt(pool.submit(() -> {
            Object data = null;
            String errMsg = null;
            try {
                data = callable.get();
            } catch (Exception e) {
                errMsg = e.getMessage();
            }
            return new AsyncResult(data,errMsg);
        }));
    }

    @Override
    public byte[] getZip() {
        return zip;
    }



    @Override
    public void interrupt() {
        pool.shutdown();
    }





}
