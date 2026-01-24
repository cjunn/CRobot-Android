package com.crobot.debug.rpc;

import java.lang.reflect.Proxy;

public class ClientApiBuilder {
    public static <T extends ClientApi> T build(Class<T> apiClz, RpcService service) {
        return (T) Proxy.newProxyInstance(
                ClientApiBuilder.class.getClassLoader(), new Class[]{apiClz}, (proxy, method, args) -> {
                    String methodName = method.getName();
                    return service.request(methodName, ParamBuild.buildClientMap(method, args));
                }
        );
    }

}
