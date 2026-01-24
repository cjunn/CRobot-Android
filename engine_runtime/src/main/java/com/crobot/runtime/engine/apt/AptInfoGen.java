package com.crobot.runtime.engine.apt;

import com.crobot.runtime.engine.apt.anno.Caller;
import com.crobot.runtime.engine.apt.anno.Execute;
import com.crobot.runtime.engine.apt.anno.Gc;
import com.crobot.runtime.engine.apt.anno.Invoke;
import com.crobot.runtime.engine.apt.anno.Value;
import com.crobot.utils.JdkSignUtil;
import com.crobot.utils.ReflectUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class AptInfoGen {
    private final static Map<String, Map<Class, Object>> CACHE = new ConcurrentHashMap<>();
    private final static String __EXECUTE = "__execute";

    private AptInfoGen() {

    }

    public static Map<Class, Object> getCache(String key) {
        return CACHE.computeIfAbsent(key, (_key) -> new ConcurrentHashMap<>());
    }

    public static JniFuncApt buildAdapterInfo(FuncApt baseApt) {
        Class<? extends FuncApt> clz = baseApt.getClass();
        Object[] objects = {
                baseApt,
                buildGcMethod(clz),
                buildInvokeMethod(clz),
                buildAllMethod(clz)
        };
        return new JniFuncApt(objects);
    }

    public static JniObjApt buildSelfAdapterInfo(ObjApt baseApt) {
        Object[] objects = {baseApt};
        return new JniObjApt(objects);
    }

    public static JniObjApt buildAdapterInfo(ObjApt baseApt) {
        Class<? extends ObjApt> clz = baseApt.getClass();
        Object[] objects = {
                baseApt,
                buildGcMethod(clz),
                buildInvokeMethod(clz),
                buildAllMethod(clz),
                buildAllField(baseApt)
        };
        return new JniObjApt(objects);
    }


    /**
     * 获取Gc方法信息[Gc混淆函数名,Gc函数签名]
     */
    private static Object buildGcMethod(Class clz) {
        return getCache("GcMethod").computeIfAbsent(clz, (_clz) -> {
            Method method = ReflectUtil.getMethods(_clz, Gc.class).get(0);
            return new Object[]{method.getName(), JdkSignUtil.getSignature(method)};
        });
    }

    /**
     * 获取Invoke方法信息[Gc混淆函数名,Gc函数签名]
     */
    private static Object buildInvokeMethod(Class clz) {
        return getCache("InvokeMethod").computeIfAbsent(clz, (_clz) -> {
            Method method = ReflectUtil.getMethods(_clz, Invoke.class).get(0);
            return new Object[]{method.getName(), JdkSignUtil.getSignature(method)};
        });
    }



    private static Object[] methodSign(Method method, String name) {
        return new Object[]{method, name, EngineSignGen.getSignature(method)};
    }

    /**
     * 获取适配方法名数组[[Method,Lua函数名,引擎签名]]
     */
    private static Object buildAllMethod(Class clz) {
        return getCache("AllMethod").computeIfAbsent(clz, (_clz) -> {
            Stream<Object[]> s1 = ReflectUtil.getMethods(clz, Caller.class)
                    .stream().map(method -> methodSign(method, method.getAnnotation(Caller.class).value()));
            Stream<Object[]> s2 = ReflectUtil.getMethods(clz, Execute.class)
                    .stream().map(method -> methodSign(method, __EXECUTE));
            Object[] objects = Stream.concat(s1, s2).toArray();
            return objects;
        });
    }

    private static Object buildAllField(Object baseApt) {
        Field[] fields = buildAllField(baseApt.getClass());
        Object[] ret = new Object[fields.length];
        for (int i = 0; i < ret.length; i++) {
            Field field = fields[i];
            String name = field.getAnnotation(Value.class).value();
            Object value = ReflectUtil.getValue(baseApt, field);
            ret[i] = new Object[]{name, value};
        }
        return ret;
    }

    private static Field[] buildAllField(Class clz) {
        return (Field[]) getCache("AllField").computeIfAbsent(clz, (_clz) -> {
            List<Field> fields = ReflectUtil.getFields(clz, Value.class);
            return fields.toArray(new Field[fields.size()]);
        });
    }

}
