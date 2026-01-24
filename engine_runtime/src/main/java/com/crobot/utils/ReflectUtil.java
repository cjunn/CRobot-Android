package com.crobot.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReflectUtil {
    private ReflectUtil() {

    }

    public static Constructor getConstructor(Class clz, Class<?>... parameterTypes) {
        try {
            return clz.getDeclaredConstructor(parameterTypes);
        } catch (Exception e) {
            throw new RuntimeException("Stub!");
        }
    }

    public static List<Method> getMethods(Class<?> clz, Class ann) {
        Map<String, Method> methods = new HashMap<>();
        do {
            getMethods(clz, ann, methods);
            getSuperMethods(clz.getInterfaces(), ann, methods);
        } while ((clz = clz.getSuperclass()) != null);
        return new ArrayList<>(methods.values());
    }

    public static List<Field> getFields(Class<?> clz, Class ann) {
        Map<String, Field> fields = new HashMap<>();
        do {
            getFields(clz, ann, fields);
        } while ((clz = clz.getSuperclass()) != null);
        return new ArrayList<>(fields.values());
    }

    public static Object getValue(Object object,Field field){
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }



    private static void getSuperMethods(Class<?>[] interfaces, Class ann, Map<String, Method> methods) {
        if (interfaces == null || interfaces.length == 0) {
            return;
        }
        for (Class<?> inter : interfaces) {
            getMethods(inter, ann, methods);
            getSuperMethods(inter.getInterfaces(), ann, methods);
        }
    }

    private static void getMethods(Class<?> clz, Class ann, Map<String, Method> methods) {
        Method[] declaredMethods = clz.getDeclaredMethods();
        for (Method method : declaredMethods) {
            method.setAccessible(true);
            String methodName = method.getName();
            if (!methods.containsKey(methodName) && method.getAnnotation(ann) != null) {
                methods.put(methodName, method);
            }
        }
    }

    private static void getFields(Class<?> clz, Class ann, Map<String, Field> methods) {
        Field[] declaredFields = clz.getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            if (!methods.containsKey(fieldName) && field.getAnnotation(ann) != null) {
                methods.put(fieldName, field);
            }
        }
    }



}
