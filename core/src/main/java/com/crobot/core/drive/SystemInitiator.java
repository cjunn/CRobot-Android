package com.crobot.core.drive;

import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;

import java.util.UUID;

public class SystemInitiator implements Initiator {

    public SystemInitiator() {
    }


    @Override
    public void execute(ContextProxy context) {
        context.setObjApt("System", new ObjApt() {
            @Caller("currentTimeMillis")
            public Number currentTimeMillis() {
                return System.currentTimeMillis();
            }

            @Caller("nanoTime")
            public Number nanoTime() {
                return System.nanoTime();
            }

            @Caller("uuid")
            public String uuid() {
                return UUID.randomUUID().toString();
            }
        });
    }
}
