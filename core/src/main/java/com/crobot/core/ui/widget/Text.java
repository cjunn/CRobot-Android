package com.crobot.core.ui.widget;

import android.content.Context;

import androidx.appcompat.widget.AppCompatTextView;

import com.crobot.core.ui.core.SimpleUISupport;

import java.util.Map;

public class Text extends SimpleUISupport<AppCompatTextView> {

    public Text(Context context, Map<String, String> attr) {
        super(context, attr);
    }

    @Override
    protected AppCompatTextView initView(Context context) {
        AppCompatTextView textView = new AppCompatTextView(context);
        textView.setSingleLine(true);
        return textView;
    }

}
