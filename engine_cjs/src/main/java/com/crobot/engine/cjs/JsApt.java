package com.crobot.engine.cjs;

import com.crobot.runtime.engine.AsyncApt;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.NameFactory;
import com.crobot.runtime.engine.ObjectPtr;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;

import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class JsApt extends ObjApt {
    private ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 30,
            30, TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new NameFactory("Async"));
    private ExecutorCompletionService<Object[]> executor = new ExecutorCompletionService(pool);

    public JsApt(ContextProxy context) {

    }

    @Caller("wrap")
    public void wrap(ObjApt asyncApt, ObjectPtr resolve, ObjectPtr reject) {
        if (!(asyncApt instanceof AsyncApt)) {
            throw new RuntimeException(String.format("The %d expectation is a %s value!", 1, "AsyncApt"));
        }
        executor.submit(() -> {
            try {
                Object data = ((AsyncApt) asyncApt).take(Long.MAX_VALUE);
                return new Object[]{data, null, resolve, reject};
            } catch (Exception e) {
                return new Object[]{null, e.getMessage(), resolve, reject};
            }
        });
    }

    @Caller("loopTake")
    public Object[] loopTake() throws Exception {
        return executor.take().get(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }


}
