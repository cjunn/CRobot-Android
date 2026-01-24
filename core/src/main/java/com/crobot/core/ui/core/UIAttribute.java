package com.crobot.core.ui.core;

public class UIAttribute {
    private final String value;

    public UIAttribute(String value) {
        this.value = value;
    }

    public Boolean getBool() {
        return Boolean.parseBoolean(this.value);
    }

    public Integer getInt() {
        try {
            return Integer.parseInt(this.value);
        } catch (Exception e) {
            return 0;
        }
    }

    public String getString() {
        return this.value;
    }

}
