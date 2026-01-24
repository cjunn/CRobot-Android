package com.crobot.runtime;

import com.crobot.runtime.engine.Result;

import java.util.concurrent.Future;

public interface ScriptRuntime {
    Future<Result> start(byte[] zip, String module, String func);
    Future<Result> start(String cmdline);
    Future<Void> stop();
}
