package com.crobot.runtime.engine.apt;

import com.crobot.runtime.engine.boot.BootUtil;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

public class JniBaseApt {
    private Object[] objects;

    public JniBaseApt(Object[] objects) {
        this.objects = objects;
    }


    @GetObjects
    public Object[] getObjects() {
        return objects;
    }

    @Inherited
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    private @interface GetObjects {
    }

    public static Method getJniReg() {
        return BootUtil.getJniMethod(JniBaseApt.class, GetObjects.class);
    }
}
