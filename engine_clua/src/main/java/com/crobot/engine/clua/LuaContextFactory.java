package com.crobot.engine.clua;

import com.crobot.runtime.engine.ContextSupportFactory;

public class LuaContextFactory extends ContextSupportFactory {

    public LuaContextFactory() {
        super(() -> new LuaContext());
    }

}
