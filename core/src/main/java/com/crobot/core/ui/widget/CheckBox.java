package com.crobot.core.ui.widget;

import android.content.Context;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;

import com.crobot.core.resource.My;
import com.crobot.core.ui.core.SimpleUISupport;
import com.crobot.core.ui.core.UIValueSetter;

import java.util.Map;

public class CheckBox extends SimpleUISupport<AppCompatCheckBox,Boolean> {

    public CheckBox(Context context, Map<String, String> attr) {
        super(context, attr);
    }

    @Override
    protected AppCompatCheckBox initView(Context context) {
        AppCompatCheckBox checkBox = new AppCompatCheckBox(context);
        checkBox.setButtonDrawable(My.style.checkbox_selector);
        checkBox.setPadding(toPx(3), 0, toPx(3), 0);
        checkBox.setTextColor(ContextCompat.getColor(context, My.color.main));
        return checkBox;
    }

    @Override
    protected void setValue(AppCompatCheckBox view, Boolean value) {
        view.setChecked(value);
    }


    @Override
    protected void bindViewValueChange(AppCompatCheckBox checkBox, UIValueSetter setter) {
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> setter.apply(isChecked));
    }
}
