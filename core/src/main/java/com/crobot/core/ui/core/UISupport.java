package com.crobot.core.ui.core;

import android.view.View;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface UISupport<V extends View> {
    V getView();

    Supplier<UIValue> getValue();

    void setValue(Object value);

    String getId();

    void initiator();

    void addValueChangeListener(Consumer<UIKeyValue> consumer);

    <K1 extends View> void addChild(UISupport<K1> c);
}
