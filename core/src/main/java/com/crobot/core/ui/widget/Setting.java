package com.crobot.core.ui.widget;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.crobot.core.ui.core.SimpleUISupport;
import com.crobot.core.ui.core.UISupport;

import java.util.Map;

public class Setting extends SimpleUISupport<LinearLayout> {
    public Setting(Context context, Map<String, String> attr) {
        super(context, attr);
    }

    @Override
    protected boolean isContainer() {
        return true;
    }

    @Override
    protected LinearLayout initView(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        return linearLayout;
    }

    @Override
    public <K1 extends View> void addChild(UISupport<K1> c) {
        super.addChild(c);
        getView().addView(c.getView());
    }

}
