package com.crobot.core.ui.core;

import android.view.ViewGroup;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface UIContext {
    void bindUIValueListen(Consumer<UIKeyValue> listen);

    void bindUIValueInit(Supplier<Map<String, Object>> initSupplier);

    void bindViewGroup(Supplier<ViewGroup> supplier);

    UIValue getUIValue(String id);

    boolean hasUI(String id);

    void flushUI(String xml);
}
