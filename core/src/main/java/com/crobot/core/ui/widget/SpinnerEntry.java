package com.crobot.core.ui.widget;

import android.content.Context;
import android.view.View;

import com.crobot.core.ui.core.Setter;
import com.crobot.core.ui.core.SimpleUISupport;

import java.util.Map;

public class SpinnerEntry extends SimpleUISupport<View> {

    private Runnable observer;
    private String key;
    private String text;

    public SpinnerEntry(Context context, Map<String, String> attr) {
        super(context, attr);
    }

    protected void setObserver(Runnable observer) {
        this.observer = observer;
    }

    public String getKey() {
        return key;
    }

    @Setter("key")
    public void setKey(String key) {
        this.key = key;
        if (this.observer != null) {
            this.observer.run();
        }
    }

    public String getText() {
        return text;
    }

    @Setter("text")
    @Override
    public void setText(String text) {
        this.text = text;
        if (this.observer != null) {
            this.observer.run();
        }
    }

    @Override
    protected View initView(Context context) {
        return new View(context);
    }


}
