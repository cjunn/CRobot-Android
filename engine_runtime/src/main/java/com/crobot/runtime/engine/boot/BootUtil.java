package com.crobot.runtime.engine.boot;
import com.crobot.runtime.engine.ContextException;
import com.crobot.runtime.engine.Varargs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
public class BootUtil {
    public static Method getJniMethod(Class clz, Class<? extends Annotation> annotationType) {
        Method[] methods = clz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(annotationType)) {
                return method;
            }
        }
        return null;
    }

    public static Constructor<?> getJniConstructor(Class clz) {
        Constructor<?>[] constructors = clz.getConstructors();
        for (Constructor constructor : constructors) {
            return constructor;
        }
        return null;
    }

}
