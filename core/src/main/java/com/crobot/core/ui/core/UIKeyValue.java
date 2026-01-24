package com.crobot.core.ui.core;


public class UIKeyValue {
    private final String id;
    private final UIValue value;

    public UIKeyValue(String id, UIValue value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public UIValue getValue() {
        return value;
    }
}
