package com.crobot.runtime.engine;


import com.crobot.runtime.engine.apt.FuncApt;
import com.crobot.runtime.engine.apt.ObjApt;

import java.nio.Buffer;
import java.util.concurrent.Callable;

public interface Context extends AsyncExecutor {
    void init(byte[] zip);

    byte[] getZip();

    void setLong(String key, long value);

    void setDouble(String key, double value);

    void setString(String key, String value);

    void setBytes(String key, byte[] value);

    void setBool(String key, boolean value);

    void setFuncApt(String key, FuncApt value);

    void setObjApt(String key, ObjApt value);

    Result start(String module, String func);

    Result start(String cmdline, Varargs varargs);

    Result start(Function function, Varargs varargs);

    Result callback(CallBack callBack, Varargs varargs);

    void close();

    void interrupt();

    void setBitmap(int width, int height, int rowStride, int pixelStride, Buffer buffer);

}
