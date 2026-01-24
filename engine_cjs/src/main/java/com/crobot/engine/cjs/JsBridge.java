package com.crobot.engine.cjs;

import android.boostrap.CBootBind;

import com.crobot.runtime.engine.apt.JniFuncApt;
import com.crobot.runtime.engine.apt.JniObjApt;
import com.crobot.runtime.engine.boot.BootInitiator;
import com.crobot.utils.CLog;

import java.nio.Buffer;

public class JsBridge {
    static {
        BootInitiator.initNative(JsBridge.class);
        System.loadLibrary("engine");
    }

    @CBootBind(100)
    public static native long newJsRuntime(JsContext object,byte[] zip);

    @CBootBind(101)
    static native void closeJsRuntime(long nativeJs);

    @CBootBind(102)
    static native void interrupt(long nativeJs);

    @CBootBind(103)
    static native Object execute(long nativeJs, String module, String func);
    @CBootBind(104)
    static native Object executeCmdline(long nativeLua, String cmdLine, Object[] args);

    @CBootBind(105)
    static native void setLong(long nativeJs, String key, long value);

    @CBootBind(106)
    static native void setDouble(long nativeJs, String key, double value);

    @CBootBind(107)
    static native void setString(long nativeJs, String key, String value);

    @CBootBind(108)
    static native void setBytes(long nativeJs, String key, byte[] value);

    @CBootBind(109)
    static native void setBool(long nativeJs, String key, boolean value);

    @CBootBind(110)
    static native void setFuncApt(long nativeJs, String key, JniFuncApt value);

    @CBootBind(111)
    static native void setObjApt(long nativeJs, String key, JniObjApt value);

    @CBootBind(112)
    static native void setBitmap(long nativeJs, int width, int height, int rowStride, int pixelStride, Buffer buffer);
    @CBootBind(113)
    static native Object executeFunction(long nativeJs, byte[] code,String fileName,int lineNumber, Object[] args);

    @CBootBind(114)
    static native Object callback(long nativeJs, long callbackHold, Object[] args);

}
