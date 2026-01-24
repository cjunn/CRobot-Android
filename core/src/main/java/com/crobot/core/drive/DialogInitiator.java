package com.crobot.core.drive;

import com.crobot.core.infra.tool.Dialog;
import com.crobot.core.util.Latch;
import com.crobot.runtime.engine.AsyncApt;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class DialogInitiator implements Initiator {
    private Dialog dialog;

    public DialogInitiator(Dialog dialog) {
        this.dialog = dialog;
    }

    @Override
    public void execute(ContextProxy context) {
        context.setObjApt("Dialog", new DialogApt(context));
    }

    public class DialogApt extends ObjApt {
        private ContextProxy context;

        public DialogApt(ContextProxy context) {
            this.context = context;
            context.addClosingEvent(() -> dialog.clear());
        }

        @Caller("alert")
        public AsyncApt alert(String title, String content) {
            Latch latch = new Latch(1);
            dialog.alert(title, content, () -> latch.countDown());
            return context.submitTask(() -> {
                latch.await();
                return null;
            });
        }

        @Caller("confirm")
        public AsyncApt confirm(String title, String content) {
            Latch latch = new Latch(1);
            AtomicBoolean result = new AtomicBoolean(false);
            dialog.confirm(title, content, (b) -> {
                result.set(b);
                latch.countDown();
            });
            return context.submitTask(() -> {
                latch.await();
                return result.get();
            });
        }

        @Caller("input")
        public AsyncApt input(String title, String content) {
            Latch latch = new Latch(1);
            AtomicReference<String> result = new AtomicReference("");
            dialog.input(title, content, (b) -> {
                result.set(b);
                latch.countDown();
            });
            return context.submitTask(() -> {
                latch.await();
                return result.get();
            });
        }
    }
}
