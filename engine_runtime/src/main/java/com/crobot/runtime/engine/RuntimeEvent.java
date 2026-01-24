package com.crobot.runtime.engine;

public interface RuntimeEvent {
    void startEvent();

    void stopEvent();

    void errorEvent(Exception exception);

    void successEvent();

    void joinEvent();

}
