package com.crobot.core.ui.core;

import android.content.Context;

import com.crobot.core.ui.widget.Button;
import com.crobot.core.ui.widget.CheckBox;
import com.crobot.core.ui.widget.Divider;
import com.crobot.core.ui.widget.HorLayout;
import com.crobot.core.ui.widget.Input;
import com.crobot.core.ui.widget.RadioButton;
import com.crobot.core.ui.widget.RadioGroup;
import com.crobot.core.ui.widget.Setting;
import com.crobot.core.ui.widget.SpinnerEntry;
import com.crobot.core.ui.widget.SpinnerList;
import com.crobot.core.ui.widget.TabPanel;
import com.crobot.core.ui.widget.Text;
import com.crobot.core.ui.widget.ViewPager;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class UIRegister {
    private static Map<String, Class<? extends UISupportImpl>> UIS = new HashMap<>();
    private static Map<Class<? extends UISupportImpl>, Map<String, Method>> SETTER = new HashMap<>();

    static {
        initMap("Button", Button.class);
        initMap("Input", Input.class);
        initMap("HorLayout", HorLayout.class);
        initMap("Setting", Setting.class);
        initMap("Text", Text.class);
        initMap("CheckBox", CheckBox.class);
        initMap("RadioButton", RadioButton.class);
        initMap("RadioGroup", RadioGroup.class);
        initMap("SpinnerEntry", SpinnerEntry.class);
        initMap("SpinnerList", SpinnerList.class);
        initMap("TabPanel", TabPanel.class);
        initMap("ViewPager", ViewPager.class);
        initMap("Divider", Divider.class);
    }

    private static void initMap(String name, Class<? extends UISupportImpl> clz) {
        UIS.put(name, clz);
        SETTER.put(clz, Arrays.asList(clz.getMethods()).stream()
                .filter(method -> method.isAnnotationPresent(Setter.class) && method.getParameterTypes().length == 1)
                .collect(Collectors.toMap(
                        k -> k.getAnnotation(Setter.class).value(),
                        v -> {
                            v.setAccessible(true);
                            return v;
                        },
                        (k1, k2) -> k1)));
    }

    private static void invoke(Method method, UISupportImpl ui, Object arg) {
        try {
            method.invoke(ui, arg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void setUIAttr(UISupportImpl ui, String attr, String value) {
        if (ui == null) {
            return;
        }
        Map<String, Method> methodMap = SETTER.get(ui.getClass());
        if (methodMap == null) {
            return;
        }
        Method method = methodMap.get(attr);
        if (method == null) {
            return;
        }
        Class<?> type = method.getParameterTypes()[0];
        invoke(method, ui, toUIAttribute(type, value));
    }

    public static UISupport newInstance(String name, Context context, Map<String, String> attrMap) {
        Class<? extends UISupportImpl> clz = UIS.get(name);
        return newInstance(clz, context, attrMap);
    }

    private static UISupport newInstance(Class<? extends UISupportImpl> clz, Context context, Map<String, String> attrMap) {
        try {
            return clz.getConstructor(Context.class, Map.class).newInstance(context, attrMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object toUIAttribute(Class type, String value) {
        if (type.equals(String.class)) {
            return value;
        }
        if (type.equals(long.class) || type.equals(Long.class)) {
            return Long.valueOf(value);
        }
        if (type.equals(int.class) || type.equals(Integer.class)) {
            return Integer.valueOf(value);
        }
        if (type.equals(short.class) || type.equals(Short.class)) {
            return Short.valueOf(value);
        }
        if (type.equals(double.class) || type.equals(Double.class)) {
            return Double.valueOf(value);
        }
        if (type.equals(float.class) || type.equals(Float.class)) {
            return Float.valueOf(value);
        }
        if (type.equals(byte.class) || type.equals(Byte.class)) {
            return Byte.valueOf(value);
        }
        if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            return Boolean.valueOf(value);
        }
        if (type.equals(char.class) || type.equals(Character.class)) {
            return Character.valueOf(value.charAt(0));
        }
        return null;
    }
}
