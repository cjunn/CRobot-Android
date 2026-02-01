package com.crobot.core.ui.core;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public abstract class UISupportImpl<V extends View, E> implements UISupport<V, E> {
    private final static AtomicInteger cnt = new AtomicInteger(1);
    private final static String ID = "id";
    protected final List<UISupport> children = new ArrayList<>();
    private final String id;
    private V view;
    private Context context;
    private E value;
    private List<Consumer<UIKeyValue>> valueChangeListeners = new ArrayList<>();

    public UISupportImpl(Context context, Map<String, String> attr) {
        this.context = context;
        this.view = initView(context);
        this.id = attr.computeIfAbsent(ID, _id -> "ui_" + cnt.incrementAndGet());
        this.initAttrValue(attr);
    }


    @Override
    public V getView() {
        return this.view;
    }

    @Override
    public void addValueChangeListener(Consumer<UIKeyValue> consumer) {
        this.valueChangeListeners.add(consumer);
    }

    @Override
    public E getValue() {
        return value;
    }

    @Override
    public void setValue(E value) {
        if (Objects.equals(this.value, value)) {
            return;
        }
        this.value = value;
        setValue(this.view, this.value);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public <K1 extends View, E> void addChild(UISupport<K1, E> c) {
        if (this.isContainer()) {
            this.children.add(c);
        }
    }

    protected abstract boolean isContainer();

    protected abstract V initView(Context context);

    protected abstract void setValue(V view, E value);

    protected abstract void setDefaultValue(V view, UIAttribute value);

    protected abstract void bindViewValueChange(V v, UIValueSetter<E> setter);

    private void initAttrValue(Map<String, String> attr) {
        Set<Map.Entry<String, String>> entries = attr.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            UIRegister.setUIAttr(this, entry.getKey(), entry.getValue());
        }
    }

    private void initValueChangeListener() {
        this.bindViewValueChange(view, value -> {
            this.value = value;
            for (Consumer<UIKeyValue> listener : valueChangeListeners) {
                listener.accept(new UIKeyValue(this.getId(), this.value));
            }
        });
    }


    @Override
    public void initiator() {
        this.initValueChangeListener();
    }

    protected int toPx(float dp) {
        float density = this.context.getResources().getDisplayMetrics().density;
        int px = (int) (dp * density + 0.5f);
        return px;
    }

}
