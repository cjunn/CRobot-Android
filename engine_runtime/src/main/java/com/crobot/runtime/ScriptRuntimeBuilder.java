package com.crobot.runtime;

import com.crobot.runtime.engine.ContextFactory;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.RuntimeEvent;

import java.util.ArrayList;
import java.util.List;

public class ScriptRuntimeBuilder {
    private ContextFactory contextFactory;
    private RuntimeEvent runtimeEvent;
    private List<Initiator> initiators = new ArrayList<>();

    public static ScriptRuntimeBuilder getBuilder() {
        return new ScriptRuntimeBuilder();
    }

    public ScriptRuntimeBuilder addInitiator(Initiator initiator) {
        this.initiators.add(initiator);
        return this;
    }

    public ScriptRuntimeBuilder addInitiator(List<Initiator> initiators) {
        this.initiators.addAll(initiators);
        return this;
    }

    public ScriptRuntimeBuilder setContextFactory(ContextFactory contextFactory) {
        this.contextFactory = contextFactory;
        return this;
    }

    public ScriptRuntimeBuilder setRuntimeEvent(RuntimeEvent runtimeEvent) {
        this.runtimeEvent = runtimeEvent;
        return this;
    }

    public ScriptRuntime builder() {
        return new ScriptRuntimeImpl(this.contextFactory, this.initiators, this.runtimeEvent);
    }


}
