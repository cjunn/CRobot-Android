package com.crobot.runtime.engine;

public interface ContextProxy extends Context {
    void addClosingEvent(ClosingEvent event);

    Result getResult(long timeout) throws Exception;

    boolean isDone();
}
