package com.crobot.engine.clua;

import com.crobot.runtime.engine.CallBack;
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

public class LuaContext extends ContextImp {
    private long nativeLua;

    public LuaContext() {
    }
    @Override
    public void init(byte[] zip) {
        super.init(zip);
        nativeLua = LuaBridge.newLuaState(this,zip);
    }

    public long getNativeLua(){
        if(nativeLua==0){
            throw new ContextException("Context尚未完成初始化!");
        }
        return nativeLua;
    }


    @Override
    public void setLong(String key, long value) {
        LuaBridge.setLong(getNativeLua(), key, value);
    }

    @Override
    public void setDouble(String key, double value) {
        LuaBridge.setDouble(getNativeLua(), key, value);
    }

    @Override
    public void setString(String key, String value) {
        LuaBridge.setString(getNativeLua(), key, value);
    }

    @Override
    public void setBytes(String key, byte[] value) {
        LuaBridge.setBytes(getNativeLua(), key, value);
    }

    @Override
    public void setBool(String key, boolean value) {
        LuaBridge.setBool(getNativeLua(), key, value);
    }

    @Override
    public void setFuncApt(String key, FuncApt value) {
        LuaBridge.setFuncApt(getNativeLua(), key, AptInfoGen.buildAdapterInfo(value));
    }

    @Override
    public void setObjApt(String key, ObjApt value) {
        LuaBridge.setObjApt(getNativeLua(), key, AptInfoGen.buildAdapterInfo(value));
    }


    @Override
    public Result start(String module, String func) {
        return new Result(LuaBridge.execute(getNativeLua(), module, func));
    }

    @Override
    public Result start(String cmdline, Varargs args) {
        return new Result(LuaBridge.executeCmdline(getNativeLua(),cmdline,args.getArgs()));
    }

    @Override
    public Result start(Function function, Varargs varargs) {
        return new Result(LuaBridge.executeFunction(getNativeLua(),function.getCode(),varargs.getArgs()));
    }

    @Override
    public Result callback(CallBack callBack, Varargs varargs) {
        return new Result(LuaBridge.callback(getNativeLua(),callBack.getHold(),varargs.getArgs()));
    }


    @Override
    public void close() {
        LuaBridge.closeLuaState(getNativeLua());
    }

    @Override
    public void interrupt() {
        LuaBridge.interrupt(getNativeLua());
        super.interrupt();
    }


    @Override
    public void setBitmap(int width, int height, int rowStride, int pixelStride, Buffer buffer) {
        LuaBridge.setBitmap(getNativeLua(), width, height, rowStride, pixelStride, buffer);
    }
}
