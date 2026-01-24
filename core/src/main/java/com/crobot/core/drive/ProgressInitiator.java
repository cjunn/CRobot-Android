package com.crobot.core.drive;

import com.crobot.core.infra.tool.Progress;
import com.crobot.runtime.engine.Context;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;

public class ProgressInitiator implements Initiator {
    private Progress progress;

    public ProgressInitiator(Progress progress) {
        this.progress = progress;
    }

    @Override
    public void execute(ContextProxy context) {
        context.setObjApt("Progress", new ObjApt() {
            @Caller("show")
            public void show(String title) {
                progress.show(title);
            }

            @Caller("setProgress")
            public void setProgress(Number value) {
                progress.setProgress(value.intValue());
            }

            @Caller("close")
            public void close() {
                progress.close();
            }

            @Override
            public int onGc(Context context) {
                progress.close();
                return super.onGc(context);
            }
        });
    }
}
