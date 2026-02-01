package com.crobot.core.ui.widget;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.crobot.core.ui.core.Setter;
import com.crobot.core.ui.core.SimpleUISupport;
import com.crobot.core.ui.core.UISupport;

import java.util.Map;

public class ViewPager extends SimpleUISupport<ScrollView, Void> {
    private LinearLayout linearLayout;
    private String title;
    private Runnable observer;


    public ViewPager(Context context, Map<String, String> attr) {
        super(context, attr);
    }

    protected void setObserver(Runnable observer) {
        this.observer = observer;
    }

    public String getTitle() {
        return title;
    }

    @Setter("title")
    public void setTitle(String title) {
        this.title = title;
        if (this.observer != null) {
            this.observer.run();
        }
    }

    @Override
    public <K1 extends View, E> void addChild(UISupport<K1, E> c) {
        super.addChild(c);
        this.linearLayout.addView(c.getView());
    }

    @Override
    protected boolean isContainer() {
        return true;
    }

    @Override
    protected ScrollView initView(Context context) {
        ScrollView scrollView = new ScrollView(context);
        this.linearLayout = new LinearLayout(context);
        this.linearLayout.setOrientation(LinearLayout.VERTICAL);
        this.linearLayout.setPadding(0, toPx(2), 0, toPx(2));
        scrollView.addView(this.linearLayout);
        return scrollView;
    }

}
