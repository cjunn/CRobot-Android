package com.crobot.runtime.engine;

import com.crobot.runtime.engine.boot.BootUtil;
import com.crobot.utils.CLog;
import com.google.gson.Gson;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

public class JsonBean {
    private static Gson gson = new Gson();
    private String json;

    public JsonBean(Object object) {
        if (object == null) {
            json = "{}";
            return;
        }
        json = gson.toJson(object);
    }

    @Inherited
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    private @interface GetJson {
    }

    @GetJson
    public String getJson() {
        return json;
    }

    public static Method getJniGetJson() {
        return BootUtil.getJniMethod(JsonBean.class, GetJson.class);
    }

    public static JsonBean create(Object object) {
        return new JsonBean(object);
    }

}
