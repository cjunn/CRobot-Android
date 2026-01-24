package com.crobot.runtime.engine;

import java.util.List;

public interface ContextFactory {
    ContextProxy newContext();
    void addInitiator(Initiator initiator);
    void addInitiators(List<Initiator> initiators);
}
