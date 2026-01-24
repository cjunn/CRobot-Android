package com.crobot.runtime.engine;

import java.util.function.Supplier;

public interface AsyncExecutor {
    <T> AsyncApt submitTask(Supplier<T> callable);

}
