package com.crobot.runtime.engine;

import com.crobot.runtime.engine.boot.BootUtil;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ObjectPtr {
    private long ptr;

    public ObjectPtr(long ptr) {
        this.ptr = ptr;
    }


    @GetPtr
    public long getPtr() {
        return ptr;
    }

    public static Constructor<?> getJniConstructor() {
        return BootUtil.getJniConstructor(ObjectPtr.class);
    }

    public static Method getJniGetPtrMethod() {
        return BootUtil.getJniMethod(ObjectPtr.class, ObjectPtr.GetPtr.class);
    }

    @Inherited
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    private @interface GetPtr {
    }

}
