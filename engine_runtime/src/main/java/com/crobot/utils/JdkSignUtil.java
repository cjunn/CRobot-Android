package com.crobot.utils;

import java.lang.reflect.Method;
import java.util.HashMap;

public class JdkSignUtil {
    private JdkSignUtil() {

    }

    private static final HashMap<String, String> BASE = new HashMap<String, String>();

    static {
        BASE.put(int.class.getName(), "I");
        BASE.put(void.class.getName(), "V");
        BASE.put(boolean.class.getName(), "Z");
        BASE.put(byte.class.getName(), "B");
        BASE.put(char.class.getName(), "C");
        BASE.put(short.class.getName(), "S");
        BASE.put(long.class.getName(), "J");
        BASE.put(float.class.getName(), "F");
        BASE.put(double.class.getName(), "D");
    }

    public static String getSignature(Method method) {
        Class<?> returnType = method.getReturnType();
        Class<?>[] parameterTypes = method.getParameterTypes();
        StringBuilder builder = new StringBuilder();
        builder.append(getParamSignature(parameterTypes));
        builder.append(getSignature(returnType));
        return builder.toString();
    }


    private static String getParamSignature(Class... params) {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (Class param : params) {
            builder.append(getSignature(param));
        }
        builder.append(")");
        return builder.toString();
    }

    private static String getSignature(Class param) {
        StringBuilder builder = new StringBuilder();
        String name;
        if (param.isArray()) {
            name = param.getComponentType().getName();
            builder.append("[");
        } else {
            name = param.getName();
        }
        if (BASE.containsKey(name)) {
            builder.append(BASE.get(name));
        } else {
            builder.append("L" + name.replace(".", "/") + ";");
        }
        return builder.toString();
    }
}
