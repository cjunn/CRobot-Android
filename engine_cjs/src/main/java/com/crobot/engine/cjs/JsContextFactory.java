package com.crobot.engine.cjs;

import com.crobot.runtime.engine.ContextSupportFactory;

public class JsContextFactory extends ContextSupportFactory {
    public JsContextFactory() {
        super(() -> new JsContext());
        this.addInitiator(context -> context.setObjApt("Apt", new JsApt(context)));
    }
}
