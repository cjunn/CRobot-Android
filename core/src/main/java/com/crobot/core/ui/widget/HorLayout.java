package com.crobot.core.ui.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.crobot.core.ui.core.Setter;
import com.crobot.core.ui.core.SimpleUISupport;
import com.crobot.core.ui.core.UISupport;

import java.util.HashMap;
import java.util.Map;

public class HorLayout extends SimpleUISupport<LinearLayout, Void> {
    private final static Map<String, Integer> GravityMap = new HashMap<>();

    static {
        GravityMap.put("left", Gravity.LEFT | Gravity.BOTTOM);
        GravityMap.put("center", Gravity.CENTER | Gravity.BOTTOM);
        GravityMap.put("right", Gravity.RIGHT | Gravity.BOTTOM);
    }


    public HorLayout(Context context, Map<String, String> attr) {
        super(context, attr);
    }

    @Setter("gravity")
    public void setGravity(String gravity) {
        Integer g = GravityMap.getOrDefault(gravity, GravityMap.get("left"));
        getView().setGravity(g);
    }


    @Override
    public <K1 extends View, E> void addChild(UISupport<K1, E> c) {
        super.addChild(c);
        getView().addView(c.getView());
    }

    @Override
    protected boolean isContainer() {
        return true;
    }

    @Override
    protected LinearLayout initView(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.setPadding(0, toPx(3), 0, toPx(3));
        return linearLayout;
    }

    @Override
    public void initiator() {
        super.initiator();
        LinearLayout view = this.getView();
        int cnt = view.getChildCount();
        for (int i = 0; i < cnt; i++) {
            View child = view.getChildAt(i);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, toPx(4), 0);
            child.setLayoutParams(params);
        }
    }
}
