package com.crobot.core.ui.widget;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.crobot.core.ui.core.Setter;
import com.crobot.core.ui.core.SimpleUISupport;
import com.crobot.core.ui.core.UIAttribute;
import com.crobot.core.ui.core.UISupport;
import com.crobot.core.ui.core.UIValue;
import com.crobot.core.ui.core.UIValueSetter;

import java.util.Map;
import java.util.Objects;

public class RadioGroup extends SimpleUISupport<android.widget.RadioGroup> {


    public RadioGroup(Context context, Map<String, String> attr) {
        super(context, attr);
    }

    @Setter("orientation")
    public void setOrientation(boolean flag) {
        this.getView().setOrientation(flag ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
    }


    private RadioButton.CompatRadio getRadio(android.widget.RadioGroup view, String value) {
        RadioButton.CompatRadio child = null;
        int cnt = view.getChildCount();
        for (int i = 0; i < cnt; i++) {
            RadioButton.CompatRadio item = (RadioButton.CompatRadio) view.getChildAt(i);
            if (Objects.equals(item.getKey(), value)) {
                child = item;
                break;
            }
        }
        if (cnt > 0 && child == null) {
            child = (RadioButton.CompatRadio) view.getChildAt(0);
        }
        return child;
    }


    @Override
    public <K1 extends View> void addChild(UISupport<K1> c) {
        super.addChild(c);
        if (c instanceof RadioButton) {
            addChild((RadioButton) c);
        }
    }


    private void addChild(RadioButton c) {
        this.getView().addView(c.getView());
    }


    @Override
    protected boolean isContainer() {
        return true;
    }

    @Override
    protected android.widget.RadioGroup initView(Context context) {
        return new android.widget.RadioGroup(context);
    }

    private void setValue(android.widget.RadioGroup view, String value) {
        RadioButton.CompatRadio radio = getRadio(view, value);
        if (radio != null) {
            this.getView().check(radio.getId());
        }
    }

    @Override
    protected void setValue(android.widget.RadioGroup view, UIValue value) {
        this.setValue(view, value.getString());
    }

    @Override
    protected void setDefaultValue(android.widget.RadioGroup view, UIAttribute value) {
        this.setValue(view, value.getString());
    }

    @Override
    protected void bindViewValueChange(android.widget.RadioGroup radioGroup, UIValueSetter setter) {
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton.CompatRadio radioButton = group.findViewById(checkedId);
            if (radioButton.isChecked()) {
                setter.apply(radioButton.getKey());
            }
        });
    }
}
