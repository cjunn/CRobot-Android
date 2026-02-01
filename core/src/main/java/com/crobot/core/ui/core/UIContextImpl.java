package com.crobot.core.ui.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.util.Pair;

import com.crobot.core.util.Latch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class UIContextImpl implements UIContext {
    private final List<Supplier<ViewGroup>> viewGroups = new ArrayList<>();
    private final List<Consumer<UIKeyValue>> valueListen = new ArrayList<>();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final Context context;
    private final Map<String, UISupport> uiMap = new ConcurrentHashMap<>();
    private final Map<String, Object> valueMap = new ConcurrentHashMap<>();
    private Supplier<Map<String, Object>> initSupplier;

    public UIContextImpl(Context context) {
        this.context = context;
    }

    @Override
    public void bindUIValueListen(Consumer<UIKeyValue> listen) {
        this.valueListen.add(listen);
    }

    @Override
    public void bindUIValueInit(Supplier<Map<String, Object>> initSupplier) {
        this.initSupplier = initSupplier;
    }

    @Override
    public void bindViewGroup(Supplier<ViewGroup> supplier) {
        this.viewGroups.add(supplier);
    }

    @Override
    public Object getUIValue(String id) {
        return valueMap.get(id);
    }

    @Override
    public void setUIValue(String id, Object value) {
        Latch latch = new Latch(1);
        main.post(() -> {
            UISupport uiSupport = uiMap.get(id);
            if (uiSupport != null) {
                uiSupport.setValue(value);
            }
            latch.countDown();
        });
        latch.await();
    }

    @Override
    public boolean hasUI(String id) {
        return valueMap.containsKey(id);
    }

    private void initUIInitiator(List<UISupport> flat) {
        for (UISupport ui : flat) {
            ui.initiator();
        }
    }


    private void setUIInitValue(List<UISupport> allFlat) {
        if (initSupplier == null) {
            return;
        }
        Map<String, Object> initValMap = initSupplier.get();
        for (UISupport ui : allFlat) {
            Object initVal = initValMap.get(ui.getId());
            if (initVal != null) {
                ui.setValue(initVal);
                this.valueMap.put(ui.getId(), initVal);
            }
        }
    }

    private void addValueChangeListener(List<UISupport> allFlat) {
        Map<String, List<UISupport>> supportMap = allFlat.stream().collect(Collectors.groupingBy(k -> k.getId()));
        for (UISupport ui : allFlat) {
            ui.addValueChangeListener((Consumer<UIKeyValue>) uiProp -> {
                String id = uiProp.getId();
                Object value = uiProp.getValue();
                List<UISupport> supports = supportMap.getOrDefault(id, Collections.emptyList());
                for (UISupport support : supports) {
                    support.setValue(value);
                }
                Object oldUIValue = this.valueMap.get(uiProp.getId());
                if (!value.equals(oldUIValue)) {
                    this.valueMap.put(uiProp.getId(), value);
                    for (Consumer<UIKeyValue> listen : valueListen) {
                        listen.accept(uiProp);
                    }
                }
            });
        }
    }

    private List<Pair<ViewGroup, View>> crateUIWidget(String xml) {
        UIParse parse = new UIParse(xml, this.context);
        List<UISupport> allFlat = new ArrayList<>();
        List<Pair<ViewGroup, View>> pairs = this.viewGroups.stream().map((body) -> {
            UIParse.Result result = parse.build();
            allFlat.addAll(result.flat);
            return Pair.create(body.get(), result.root.getView());
        }).collect(Collectors.toList());
        this.addValueChangeListener(allFlat);
        this.setUIInitValue(allFlat);
        this.initUIInitiator(allFlat);
        this.initUIMap(allFlat);
        return pairs;
    }

    private void initUIMap(List<UISupport> allFlat) {
        uiMap.putAll(allFlat.stream().collect(Collectors.toMap(k -> k.getId(), k -> k, (k1, k2) -> k1)));
    }


    @Override
    public void flushUI(String xml) {
        List<Pair<ViewGroup, View>> pairs = crateUIWidget(xml);
        Latch latch = new Latch(pairs.size());
        for (Pair<ViewGroup, View> pair : pairs) {
            main.post(() -> {
                ViewGroup body = pair.first;
                View root = pair.second;
                body.removeAllViews();
                body.addView(root);
                latch.countDown();
            });
        }
        latch.await();
    }


}
