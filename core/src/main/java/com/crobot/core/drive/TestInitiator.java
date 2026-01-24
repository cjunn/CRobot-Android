package com.crobot.core.drive;

import com.crobot.runtime.engine.CallBack;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.Varargs;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;

public class TestInitiator implements Initiator {
    @Override
    public void execute(ContextProxy context) {
        context.setObjApt("Test", new ObjApt() {
            @Caller("test")
            public String encode(CallBack callBack) {
                callBack.apply(Varargs.create("321"));
                return "123";
            }
        });
    }
}
