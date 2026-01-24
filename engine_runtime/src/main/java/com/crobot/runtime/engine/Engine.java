package com.crobot.runtime.engine;

import java.util.Map;

public interface Engine {
    Result start(String module, String func);

    Result start(String cmdline, Varargs args);

    Result start(Function function, Varargs args);

    void close();
}
