package com.crobot.core.drive;

import com.crobot.core.ui.core.UIContext;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;

public class UIInitiator implements Initiator {
    private UIContext uiContext;

    public UIInitiator(UIContext uiContext) {
        this.uiContext = uiContext;
    }

    @Override
    public void execute(ContextProxy context) {
        context.setObjApt("UI", new UIApt());
    }

    private class UIApt extends ObjApt {
        @Caller("getValue")
        public Object getValue(String id) {
            return uiContext.getUIValue(id).get();
        }

        @Caller("setValue")
        public void setValue(String id, Object value) {
            uiContext.setUIValue(id, value);
        }
    }

}
