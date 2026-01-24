package com.crobot.engine.cjs;

import com.crobot.runtime.engine.CallBack;
import com.crobot.runtime.engine.Context;
import com.crobot.runtime.engine.ContextException;
import com.crobot.runtime.engine.ContextImp;
import com.crobot.runtime.engine.Function;
import com.crobot.runtime.engine.Result;
import com.crobot.runtime.engine.Varargs;
import com.crobot.runtime.engine.apt.AptInfoGen;
import com.crobot.runtime.engine.apt.FuncApt;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.utils.CLog;

import java.nio.Buffer;

public class JsContext extends ContextImp {
    private long nativeJs;

    public JsContext() {
    }

    public long getNativeJs() {
        if (nativeJs == 0) {
            throw new ContextException("Context尚未完成初始化!");
        }
        return nativeJs;
    }


    @Override
    public void init(byte[] zip) {
        super.init(zip);
        this.nativeJs = JsBridge.newJsRuntime(this, zip);
    }

    @Override
    public void setLong(String key, long value) {
        JsBridge.setLong(getNativeJs(), key, value);
    }

    @Override
    public void setDouble(String key, double value) {
        JsBridge.setDouble(getNativeJs(), key, value);
    }

    @Override
    public void setString(String key, String value) {
        JsBridge.setString(getNativeJs(), key, value);
    }

    @Override
    public void setBytes(String key, byte[] value) {
        JsBridge.setBytes(getNativeJs(), key, value);
    }

    @Override
    public void setBool(String key, boolean value) {
        JsBridge.setBool(getNativeJs(), key, value);
    }

    @Override
    public void setFuncApt(String key, FuncApt value) {
        JsBridge.setFuncApt(getNativeJs(), key, AptInfoGen.buildAdapterInfo(value));
    }

    @Override
    public void setObjApt(String key, ObjApt value) {
        JsBridge.setObjApt(getNativeJs(), key, AptInfoGen.buildAdapterInfo(value));
    }

    @Override
    public Result start(String module, String func) {
        return new Result(JsBridge.execute(getNativeJs(), module, func));
    }

    @Override
    public Result start(String cmdline, Varargs args) {
        return new Result(JsBridge.executeCmdline(getNativeJs(), cmdline, args.getArgs()));
    }

    @Override
    public Result start(Function function, Varargs varargs) {
        return new Result(JsBridge.executeFunction(getNativeJs(), function.getCode(), function.getFileName(), function.getLineNumber(), varargs.getArgs()));
    }

    @Override
    public Result callback(CallBack callBack, Varargs varargs) {
        return new Result(JsBridge.callback(getNativeJs(), callBack.getHold(), varargs.getArgs()));
    }

    @Override
    public void close() {
        JsBridge.closeJsRuntime(this.nativeJs);
    }

    @Override
    public void interrupt() {
        JsBridge.interrupt(getNativeJs());
        super.interrupt();
    }

    @Override
    public void setBitmap(int width, int height, int rowStride, int pixelStride, Buffer buffer) {
        JsBridge.setBitmap(getNativeJs(), width, height, rowStride, pixelStride, buffer);
    }

}
