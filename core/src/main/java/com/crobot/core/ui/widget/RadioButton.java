package com.crobot.core.ui.widget;

import android.content.Context;

import androidx.appcompat.widget.AppCompatRadioButton;

import com.crobot.core.resource.My;
import com.crobot.core.ui.core.Setter;
import com.crobot.core.ui.core.SimpleUISupport;

import java.util.Map;

public class RadioButton extends SimpleUISupport<RadioButton.CompatRadio> {
    private String key;

    public RadioButton(Context context, Map<String, String> attr) {
        super(context, attr);
    }

    @Setter("key")
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    protected CompatRadio initView(Context context) {
        CompatRadio compatRadio = new CompatRadio(context);
        compatRadio.setPadding(toPx(3), 0, toPx(15), 0);
        compatRadio.setButtonDrawable(My.style.radio_selector);
        return compatRadio;
    }

    protected class CompatRadio extends AppCompatRadioButton {

        public CompatRadio(Context context) {
            super(context);
        }

        public String getKey() {
            return key;
        }

    }

}
