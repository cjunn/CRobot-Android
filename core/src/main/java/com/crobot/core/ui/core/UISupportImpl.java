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
import java.util.function.Supplier;

public abstract class UISupportImpl<V extends View> implements UISupport<V> {
    private final static AtomicInteger cnt = new AtomicInteger(1);
    private final static String ID = "id";
    protected final List<UISupport> children = new ArrayList<>();
    private final String id;
    private V view;
    private Context context;
    private UIValue value = UIValue.EMPTY;
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
    public Supplier<UIValue> getValue() {
        return () -> value;
    }

    @Override
    public void setValue(Object value) {
        if (Objects.equals(this.value.get(), value)) {
            return;
        }
        this.value = new UIValue(value);
        setValue(this.view, this.value);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public <K1 extends View> void addChild(UISupport<K1> c) {
        if (this.isContainer()) {
            this.children.add(c);
        }
    }

    protected abstract boolean isContainer();

    protected abstract V initView(Context context);

    protected abstract void setValue(V view, UIValue value);

    protected abstract void setDefaultValue(V view, UIAttribute value);


    protected abstract void bindViewValueChange(V v, UIValueSetter setter);

    private void initAttrValue(Map<String, String> attr) {
        Set<Map.Entry<String, String>> entries = attr.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            UIRegister.setUIAttr(this, entry.getKey(), entry.getValue());
        }
    }

    private void initValueChangeListener() {
        this.bindViewValueChange(view, value -> {
            this.value = new UIValue(value);
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
