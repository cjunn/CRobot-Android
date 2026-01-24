package com.crobot.core.drive;

import com.crobot.core.infra.tool.Output;
import com.crobot.runtime.engine.AsyncApt;
import com.crobot.runtime.engine.Context;
import com.crobot.runtime.engine.ContextFactory;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Function;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.Varargs;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;
import com.crobot.utils.Delayer;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadInitiator implements Initiator {
    private ContextFactory contextFactory;
    private Output output;
    private Map<String, Object> valuesMap = new ConcurrentHashMap<>();

    public ThreadInitiator(ContextFactory contextFactory, Output output) {
        this.contextFactory = contextFactory;
        this.output = output;
    }

    @Override
    public void execute(ContextProxy context) {
        context.setObjApt("Thread", new ThreadObjApt(context));
    }

    public class ThreadObjApt extends ObjApt {
        private ContextProxy mContext;
        private Delayer delayer;

        public ThreadObjApt(ContextProxy mContext) {
            this.mContext = mContext;
            this.delayer = new Delayer();
            this.mContext.addClosingEvent(() -> this.delayer.stop());
        }

        @Caller("start")
        public AsyncApt start(Function function, Varargs varargs) throws Exception {
            ContextProxy cContext = contextFactory.newContext();
            CompletableFuture<Boolean> init = new CompletableFuture<>();
            AsyncApt asyncApt = mContext.submitTask(() -> {
                try {
                    byte[] zip = mContext.getZip();
                    cContext.init(zip);
                    init.complete(true);
                    return cContext.start(function, varargs).getObject();
                } catch (Exception e) {
                    output.error(e);
                    throw e;
                } finally {
                    cContext.close();
                }
            });
            init.get();
            return asyncApt;
        }


        @Caller("getValue")
        public Object getValue(String key) {
            return valuesMap.get(key);
        }

        @Caller("name")
        public String name() {
            return Thread.currentThread().getName();
        }

        @Caller("setValue")
        public void setValue(String key, Object value) {
            valuesMap.put(key, value);
        }

        @Caller("sleep")
        public void sleep(Number ms) {
            delayer.delay(ms.longValue());
        }

        @Caller("delay")
        public AsyncApt delay(Number ms) {
            return mContext.submitTask(() -> {
                try {
                    Thread.sleep(ms.longValue());
                } catch (Exception e) {

                }
                return null;
            });
        }

        @Override
        public int onGc(Context context) {
            return super.onGc(context);
        }

    }


}
