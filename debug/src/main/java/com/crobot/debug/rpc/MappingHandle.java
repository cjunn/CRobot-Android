package com.crobot.debug.rpc;

import com.crobot.core.util.StackUtil;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MappingHandle {
    private static Map<String, Handle> HandleMap = new ConcurrentHashMap<>();
    private RpcService service;

    public MappingHandle(RpcService service) {
        this.service = service;
    }

    public void register(Object handle) {
        Method[] methods = handle.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Mapping.class)) {
                method.setAccessible(true);
                HandleMap.put(method.getName(), new Handle(handle, method));
            }
        }
    }


    public void invoke(Request request) {
        Handle handle = HandleMap.get(request.getMethod());
        if (handle == null) {
            service.errorResponse(request.getUuid(), "CRobot-Service 未找到处理器[" + request.getMethod() + "]");
            return;
        }
        try {
            Object[] args = ParamBuild.buildMappingArg(handle.method, request.getData());
            Object invoke = handle.method.invoke(handle.object, args);
            service.successResponse(request.getUuid(), invoke);
        } catch (Exception e) {
            Throwable throwable = StackUtil.trackThrowable(e);
            service.errorResponse(request.getUuid(), StackUtil.getStackTrace(throwable));
        }
    }


    public static class Handle {
        protected final Object object;
        protected final Method method;

        public Handle(Object object, Method method) {
            this.object = object;
            this.method = method;
        }
    }
}
