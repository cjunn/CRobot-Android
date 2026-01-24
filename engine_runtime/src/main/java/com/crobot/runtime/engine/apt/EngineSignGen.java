package com.crobot.runtime.engine.apt;

import com.crobot.runtime.engine.CallBack;
import com.crobot.runtime.engine.Function;
import com.crobot.runtime.engine.JsonBean;
import com.crobot.runtime.engine.ObjectPtr;
import com.crobot.runtime.engine.Varargs;
import com.crobot.utils.CLog;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class EngineSignGen {
    public final static char TYPE_VOID = '0';
    public final static char TYPE_ANY = '1';
    public final static char TYPE_NUMBER = '2';
    public final static char TYPE_BOOLEAN = '3';
    public final static char TYPE_STRING = '4';
    public final static char TYPE_OBJ_APT = '5';
    public final static char TYPE_FUNCTION = '6';
    public final static char TYPE_VARARGS = '7';
    public final static char TYPE_OBJ_PTR = '8';
    public final static char TYPE_MAP = '9';
    public final static char TYPE_BYTES = 'a';
    public final static char TYPE_CALLBACK = 'b';
    public final static char TYPE_JSONBEAN = 'c';

    private EngineSignGen() {

    }

    private static final HashMap<Class, Character> PRIM = new HashMap<>();

    static {
        PRIM.put(void.class, TYPE_VOID);
        PRIM.put(Object.class, TYPE_ANY);
        PRIM.put(boolean.class, TYPE_BOOLEAN);
        PRIM.put(Boolean.class, TYPE_BOOLEAN);
        PRIM.put(Number.class,TYPE_NUMBER);
        PRIM.put(String.class, TYPE_STRING);
        PRIM.put(ObjApt.class, TYPE_OBJ_APT);
        PRIM.put(Function.class,TYPE_FUNCTION);
        PRIM.put(Varargs.class, TYPE_VARARGS);
        PRIM.put(ObjectPtr.class, TYPE_OBJ_PTR);
        PRIM.put(Map.class,TYPE_MAP);
        PRIM.put(byte[].class,TYPE_BYTES);
        PRIM.put(CallBack.class,TYPE_CALLBACK);
        PRIM.put(JsonBean.class,TYPE_JSONBEAN);
    }

    protected static String getSignature(Method method) {
        return getSignature(method,method.getReturnType(), method.getParameterTypes());
    }

    private static String getSignature(Method method,Class ret, Class... params) {
        StringBuilder builder = new StringBuilder();
        builder.append(getSignature(method,ret));
        for (Class param : params) {
            builder.append(getSignature(method,param));
        }
        return builder.toString();
    }


    private static Character getSignatureWrap(Class clz) {
        if (clz == null) {
            return null;
        }
        if (PRIM.containsKey(clz)) {
            return PRIM.get(clz);
        }
        return getSignatureWrap(clz.getSuperclass());
    }

    private static Character getSignature(Method method,Class clz) {
        Character signature = getSignatureWrap(clz);
        if (signature == null) {
            throw new RuntimeException("The Method:"+method+" type is not supported :" + clz);
        }
        return signature;
    }

}
