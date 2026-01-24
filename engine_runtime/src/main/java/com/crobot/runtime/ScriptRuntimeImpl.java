package com.crobot.runtime;

import com.crobot.runtime.engine.ContextFactory;
import com.crobot.runtime.engine.Engine;
import com.crobot.runtime.engine.EngineImp;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.Result;
import com.crobot.runtime.engine.RuntimeEvent;
import com.crobot.runtime.engine.Varargs;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ScriptRuntimeImpl implements ScriptRuntime {
    ExecutorService startSrv = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("main");
        return thread;
    });
    ExecutorService stopSrv = Executors.newSingleThreadExecutor();
    ExecutorService runSrv = Executors.newSingleThreadExecutor();
    private RuntimeEvent runtimeEvent;
    private ContextFactory contextFactory;
    private WeakReference<Engine> engineRef;
    private Future<Result> future;
    public ScriptRuntimeImpl(ContextFactory contextFactory, List<Initiator> initiators,
                             RuntimeEvent runtimeEvent) {
        this.contextFactory = contextFactory;
        this.runtimeEvent = runtimeEvent;
        this.contextFactory.addInitiators(initiators);
    }

    @Override
    public synchronized Future<Result> start(byte[] zip, String module, String func) {
        if (this.future != null && !this.future.isDone()) {
            runtimeEvent.joinEvent();
            return this.future;
        }
        EngineImp engine = new EngineImp(contextFactory, runtimeEvent, zip);
        this.engineRef = new WeakReference<>(engine);
        this.future = startSrv.submit(() -> engine.start(module, func));
        return this.future;
    }

    @Override
    public Future<Result> start(String cmdline) {
        EngineImp engine = new EngineImp(contextFactory);
        return runSrv.submit(() -> engine.start(cmdline, Varargs.Empty));
    }

    @Override
    public Future stop() {
        Engine engine = this.engineRef.get();
        if (engine != null) {
            return stopSrv.submit(() -> engine.close());
        }
        return CompletableFuture.completedFuture(null);
    }




}
