package com.crobot.engine.clua;

import android.boostrap.CBootBind;

import com.crobot.runtime.engine.apt.JniFuncApt;
import com.crobot.runtime.engine.apt.JniObjApt;
import com.crobot.runtime.engine.boot.BootInitiator;

import java.nio.Buffer;

public class LuaBridge {
    static {
        BootInitiator.initNative(LuaBridge.class);
        System.loadLibrary("engine");
    }

    @CBootBind(100)
    static native long newLuaState(Object context,byte[] zip);

    @CBootBind(101)
    static native void closeLuaState(long nativeLua);

    @CBootBind(102)
    static native void interrupt(long nativeLua);

    @CBootBind(103)
    static native Object execute(long nativeLua, String module, String func);

    @CBootBind(104)
    static native Object executeCmdline(long nativeLua, String cmdline, Object[] args);

    @CBootBind(105)
    static native void setLong(long nativeLua, String key, long value);

    @CBootBind(106)
    static native void setDouble(long nativeLua, String key, double value);

    @CBootBind(107)
    static native void setString(long nativeLua, String key, String value);

    @CBootBind(108)
    static native void setBytes(long nativeLua, String key, byte[] value);

    @CBootBind(109)
    static native void setBool(long nativeLua, String key, boolean value);

    @CBootBind(110)
    static native void setFuncApt(long nativeLua, String key, JniFuncApt value);

    @CBootBind(111)
    static native void setObjApt(long nativeLua, String key, JniObjApt value);
    @CBootBind(112)
    static native void setBitmap(long nativeLua, int width, int height, int rowStride, int pixelStride, Buffer buffer);
    @CBootBind(113)
    static native Object executeFunction(long nativeLua, byte[] code, Object[] args);

    @CBootBind(114)
    static native Object callback(long nativeLua, long hold, Object[] args);
}
