package android.boostrap;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CBootLink {
    private static int BOOT_NATIVE_FUNC = 1;

    public static synchronized void addBoot(int type, Object... params) {
        CBootstrap.LINK.put(type, params);
    }

    public static synchronized void addNative(Class clz) {
        Map regInfo = (Map) CBootstrap.LINK.computeIfAbsent(BOOT_NATIVE_FUNC, (i) -> new HashMap<>());
        Method[] allMethod = clz.getDeclaredMethods();
        for (Method method : allMethod) {
            CBootBind flag = method.getAnnotation(CBootBind.class);
            if (flag != null) {
                regInfo.put(flag.value(), new Object[]{clz, method.getName(), CBootGen.getSignature(method)});
            }
        }
    }
}
