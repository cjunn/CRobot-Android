package com.crobot.core.ui.core;

import java.util.Objects;

public class UIValue {
    public final static UIValue EMPTY = new UIValue(null);
    private final Object value;

    public UIValue(Object value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UIValue uiValue = (UIValue) o;
        return Objects.equals(value, uiValue.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    private <T> T getValue(Class clz, T def) {
        if (value == null) {
            return def;
        }
        if (value.getClass().equals(clz)) {
            return (T) value;
        }
        return def;
    }


    public Boolean getBool() {
        return getValue(Boolean.class, false);
    }

    public String getString() {
        return getValue(String.class, "");
    }

    public Integer getInt() {
        return getValue(Integer.class, 0);
    }


    public Object get() {
        return this.value;
    }
}
