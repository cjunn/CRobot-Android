package com.crobot.runtime.engine;

import com.crobot.runtime.engine.boot.BootUtil;

import java.lang.reflect.Constructor;

public class Function {
    private byte[] code;
    private String fileName;
    private int lineNumber;

    public Function(byte[] code, String fileName, int lineNumber) {
        this.code = code;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }

    public byte[] getCode() {
        return code;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public static Constructor<?> getJniConstructor() {
        return BootUtil.getJniConstructor(Function.class);
    }

}
