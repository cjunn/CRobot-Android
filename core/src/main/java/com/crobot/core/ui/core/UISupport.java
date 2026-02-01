package com.crobot.core.ui.core;

import android.view.View;

import java.util.function.Consumer;

public interface UISupport<V extends View, E> {
    V getView();

    E getValue();

    void setValue(E value);

    String getId();

    void initiator();

    void addValueChangeListener(Consumer<UIKeyValue> consumer);

    <K1 extends View, E> void addChild(UISupport<K1, E> c);
}
