package com.crobot.core.ui.widget;

import android.content.Context;

import androidx.appcompat.widget.AppCompatButton;

import com.crobot.core.ui.core.SimpleUISupport;

import java.util.Map;

public class Button extends SimpleUISupport<AppCompatButton> {


    public Button(Context context, Map<String, String> attr) {
        super(context, attr);
    }

    @Override
    protected AppCompatButton initView(Context context) {
        return new AppCompatButton(context);
    }


}
