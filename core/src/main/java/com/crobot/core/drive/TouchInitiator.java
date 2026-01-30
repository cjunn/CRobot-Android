package com.crobot.core.drive;

import com.crobot.core.infra.tool.ScreenTouch;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;

public class TouchInitiator implements Initiator {
    private ScreenTouch screenTouch;

    public TouchInitiator(ScreenTouch screenTouch) {
        this.screenTouch = screenTouch;
    }


    @Override
    public void execute(ContextProxy context) {
        context.setObjApt("Input", new ObjApt() {
            @Caller("tap")
            public void tap(Number x, Number y, Number delay) {
                screenTouch.tap(x.floatValue(), y.floatValue(), delay.intValue());
            }

            @Caller("swipe")
            public void swipe(Number x1, Number y1, Number x2, Number y2, Number duration) {
                screenTouch.swipe(x1.floatValue(), y1.floatValue(), x2.floatValue(), y2.floatValue(), duration.intValue());
            }
        });
    }
}
