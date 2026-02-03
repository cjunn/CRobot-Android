package com.crobot.core.drive;

import com.crobot.core.infra.tool.Toaster;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.Varargs;
import com.crobot.runtime.engine.apt.FuncApt;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;
import com.crobot.runtime.engine.apt.anno.Execute;

public class ToasterInitiator implements Initiator {
    private Toaster toaster;

    public ToasterInitiator(Toaster toaster) {
        this.toaster = toaster;
    }

    @Override
    public void execute(ContextProxy context) {
        context.setFuncApt("toast", new FuncApt() {
            @Execute
            public void show(String message) {
                toaster.show(message);
            }
        });
    }
}
