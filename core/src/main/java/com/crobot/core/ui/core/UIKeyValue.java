package com.crobot.core.ui.core;


public class UIKeyValue {
    private final String id;
    private final Object value;

    public UIKeyValue(String id, Object value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public Object getValue() {
        return value;
    }
}
