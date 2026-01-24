package com.crobot.runtime.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class ContextSupportFactory implements ContextFactory {
    private Supplier<Context> supplier;
    private List<Initiator> initiators = new ArrayList<>();

    public ContextSupportFactory(Supplier<Context> supplier) {
        this.supplier = supplier;
    }

    public void addInitiator(Initiator initiator) {
        this.initiators.add(initiator);
    }

    @Override
    public void addInitiators(List<Initiator> initiators) {
        for(Initiator initiator:initiators){
            this.addInitiator(initiator);
        }
    }

    @Override
    public ContextProxy newContext() {
        return new ContextSupport(supplier.get(),initiators);
    }
}
