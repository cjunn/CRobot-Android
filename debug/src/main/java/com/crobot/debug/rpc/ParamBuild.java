package com.crobot.debug.rpc;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ParamBuild {
    private static Map<Method, Param[]> methodParamMap = new ConcurrentHashMap<>();
    private static Map<Method, Class[]> methodTypeMap = new ConcurrentHashMap<>();

    private static Param toParam(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(Param.class)) {
                return ((Param) annotation);
            }
        }
        return null;
    }

    private static Param[] toParams(Method method) {
        Annotation[][] annotations = method.getParameterAnnotations();
        Param[] params = new Param[annotations.length];
        for (int i = 0; i < annotations.length; i++) {
            params[i] = toParam(annotations[i]);
        }
        return params;
    }


    public static Map<String, Object> buildClientMap(Method method, Object[] args) {
        Param[] params = methodParamMap.computeIfAbsent(method, (_method) -> toParams(_method));
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < params.length; i++) {
            if (params[i] == null) {
                continue;
            }
            map.put(params[i].value(), args[i]);
        }
        return map;
    }

    public static Object[] buildMappingArg(Method method, Map<String, Object> maps) {
        Param[] params = methodParamMap.computeIfAbsent(method, (_method) -> toParams(_method));
        Class[] types = methodTypeMap.computeIfAbsent(method, (_method) -> _method.getParameterTypes());

        Object[] ret = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            if (params[i] == null) {
                continue;
            }
            Param param = params[i];
            Class type = types[i];
            if (type.isAssignableFrom(List.class)) {
                ret[i] = new JsonBean(maps.get(param.value())).asList(param.clazz());
            } else {
                ret[i] = maps.get(param.value());
            }
        }
        return ret;
    }

}
