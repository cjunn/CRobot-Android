package com.crobot.runtime.engine;

import com.crobot.runtime.engine.apt.FuncApt;
import com.crobot.runtime.engine.apt.ObjApt;

import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ContextSupport implements ContextProxy {
    private List<ClosingEvent> closingEvents = new ArrayList<>();
    private final Context context;
    private Object lock = new Object();
    private Thread thread;
    private AtomicBoolean closed = new AtomicBoolean(false);
    private List<Initiator> initiators;
    private CompletableFuture<Result> future = new CompletableFuture<>();
    public ContextSupport(Context context, List<Initiator> initiators) {
        this.context = context;
        this.initiators = initiators;
    }


    @Override
    public void setLong(String key, long value) {
        context.setLong(key, value);
    }

    @Override
    public void setDouble(String key, double value) {
        context.setDouble(key, value);
    }

    @Override
    public void setString(String key, String value) {
        context.setString(key, value);
    }

    @Override
    public void setBytes(String key, byte[] value) {
        context.setBytes(key, value);
    }

    @Override
    public void setBool(String key, boolean value) {
        context.setBool(key, value);
    }

    @Override
    public void setFuncApt(String key, FuncApt value) {
        context.setFuncApt(key, value);
    }

    @Override
    public void setObjApt(String key, ObjApt value) {
        context.setObjApt(key, value);
    }


    @Override
    public Result start(String module, String func) {
        synchronized (lock) {
            try {
                if(closed.get()){
                    return null;
                }
                thread = Thread.currentThread();
                Result result = context.start(module, func);
                future.complete(result);
                return result;
            } finally {
                closingEvents.forEach(event -> event.execute());
                this.interrupt();
                context.close();
                closed.set(true);
            }
        }
    }

    @Override
    public Result start(String cmdline, Varargs args) {
        synchronized (lock) {
            try {
                if(closed.get()){
                    return null;
                }
                Result result = context.start(cmdline, args);
                future.complete(result);
                return result;
            }finally {
                closingEvents.forEach(event -> event.execute());
                this.interrupt();
                context.close();
                closed.set(true);
            }
        }
    }

    @Override
    public Result start(Function function, Varargs varargs) {
        synchronized (lock) {
            try {
                if(closed.get()){
                    return null;
                }
                thread = Thread.currentThread();
                Result result = context.start(function, varargs);
                future.complete(result);
                return result;
            } finally {
                closingEvents.forEach(event -> event.execute());
                this.interrupt();
                context.close();
                closed.set(true);
            }
        }
    }

    @Override
    public Result callback(CallBack callBack, Varargs varargs) {
        return context.callback(callBack,varargs);
    }

    @Override
    public void addClosingEvent(ClosingEvent event) {
        closingEvents.add(event);
    }

    @Override
    public Result getResult(long timeout) throws Exception {
        return future.get(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public void close() {
        if (closed.get()) {
            return;
        }
        //进行中断，然后等待主线程执行完毕
        this.interrupt();
        closingEvents.forEach(event -> event.execute());
        synchronized (lock) {
        }
    }

    @Override
    public void interrupt() {
        context.interrupt();
        thread.interrupt();
    }

    @Override
    public byte[] getZip() {
        return context.getZip();
    }

    @Override
    public void init(byte[] zip) {
        thread = Thread.currentThread();
        context.init(zip);
        for (Initiator initiator : initiators) {
            initiator.execute(this);
        }
    }

    @Override
    public void setBitmap(int width, int height, int rowStride, int pixelStride, Buffer buffer) {
        context.setBitmap(width, height, rowStride, pixelStride, buffer);
    }


    @Override
    public <T> AsyncApt submitTask(Supplier<T> callable) {
        return context.submitTask(callable);
    }


}
