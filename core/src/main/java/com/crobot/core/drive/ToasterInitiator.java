package com.crobot.core.drive;

import com.crobot.core.infra.tool.Toaster;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;

public class ToasterInitiator implements Initiator {
    private Toaster toaster;

    public ToasterInitiator(Toaster toaster) {
        this.toaster = toaster;
    }

    @Override
    public void execute(ContextProxy context) {
        context.setObjApt("Toaster", new ObjApt() {
            @Caller("show")
            public void show(String message) {
                toaster.show(message);
            }
        });
    }
}
