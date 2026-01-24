package com.crobot.runtime.engine;

@FunctionalInterface
public interface AsyncCallback<V> {
    V call(String uid) throws Exception;
}
