package com.crobot.runtime.engine;

import com.crobot.runtime.engine.boot.BootUtil;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

public class Varargs {
    public static Varargs Empty = new Varargs(new Object[]{});

    public Varargs(Object[] args) {
        this.args = args;
    }

    public Object[] getArgs() {
        return args;
    }

    private Object[] args;

    public static Constructor<?> getJniConstructor() {
        return BootUtil.getJniConstructor(Varargs.class);
    }

    public <T> T getValue(int idx, Class<T> clz) {
        if (idx >= args.length || args[idx] == null) {
            return null;
        }
        if (!args[idx].getClass().isAssignableFrom(clz)) {
            return null;
        }
        return (T) args[idx];
    }

    public <T> T getValue(int idx,Class<T> clz, Supplier<T> def) {
        if (idx >= args.length || args[idx] == null) {
            return def.get();
        }
        if (!args[idx].getClass().isAssignableFrom(clz)) {
            return def.get();
        }
        return (T) args[idx];
    }

    public <T> T getValue(int idx, T def) {
        if (idx >= args.length || args[idx] == null) {
            return def;
        }
        if (!args[idx].getClass().isAssignableFrom(def.getClass())) {
            return def;
        }
        return (T) args[idx];
    }

    public static Varargs create(Object... args){
        return new Varargs(args);
    }

}
