package com.crobot.core.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;

import com.crobot.core.resource.My;
import com.crobot.core.ui.core.Setter;
import com.crobot.core.ui.core.SimpleUISupport;
import com.crobot.core.ui.core.UIAttribute;
import com.crobot.core.ui.core.UIValueSetter;
import com.crobot.utils.CLog;

import java.lang.reflect.Field;
import java.util.Map;

public class Input extends SimpleUISupport<Input.MyEditText, String> {

    public Input(Context context, Map<String, String> attr) {
        super(context, attr);
    }

    @Setter("minWidth")
    public void setMinWidth(int minWidth) {
        this.getView().setMinWidth(minWidth);
    }

    @Setter("placeholder")
    public void setPlaceholder(String text) {
        this.getView().setHint(text);
    }


    @Override
    protected MyEditText initView(Context context) {
        return new MyEditText(context);
    }

    @Override
    protected void setValue(MyEditText view, String value) {
        view.setText(value);
    }

    @Override
    protected void setDefaultValue(MyEditText view, UIAttribute value) {
        this.setValue(view, value.getString());
    }

    @Override
    protected void bindViewValueChange(MyEditText myEditText, UIValueSetter setter) {
        myEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setter.apply(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public class MyEditText extends AppCompatEditText {
        public MyEditText(@NonNull Context context) {
            super(context);
            this.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            this.setPadding(toPx(6), 2, toPx(6), 2);
            this.setCursorDrawable(My.style.input_curse);
            this.setBackgroundResource(My.style.input_selector);
            this.setSingleLine();
            this.setMinWidth(50);
        }

        @SuppressLint("SoonBlockedPrivateApi")
        public void setCursorDrawable(int resID) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                this.setTextCursorDrawable(resID);
            } else {
                try {
                    Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
                    mCursorDrawableRes.setAccessible(true);
                    mCursorDrawableRes.setInt(this, resID);
                    this.invalidate();
                } catch (Exception e) {
                    CLog.error("setCursorDrawable", e);
                }
            }
        }
    }

}
