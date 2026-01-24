package android.boostrap;

import java.lang.reflect.Method;
import java.util.HashMap;

public class CBootGen {
    private static final HashMap<String, String> Primitives = new HashMap<String, String>();

    static {
        Primitives.put(void.class.getName(), "V");
        Primitives.put(boolean.class.getName(), "Z");
        Primitives.put(byte.class.getName(), "B");
        Primitives.put(char.class.getName(), "C");
        Primitives.put(short.class.getName(), "S");
        Primitives.put(int.class.getName(), "I");
        Primitives.put(long.class.getName(), "J");
        Primitives.put(float.class.getName(), "F");
        Primitives.put(double.class.getName(), "D");
    }

    public static String getSignature(Method method) {
        return getSignature(method.getReturnType(), method.getParameterTypes());
    }

    private static String getSignature(Class ret, Class... params) {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (Class param : params) {
            builder.append(getSignature(param));
        }
        builder.append(")");
        builder.append(getSignature(ret));
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
        if (Primitives.containsKey(name)) {
            builder.append(Primitives.get(name));
        } else {
            builder.append("L" + name.replace(".", "/") + ";");
        }
        return builder.toString();
    }

}
