package com.crobot.core.ui.core;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import java.util.Map;

public abstract class SimpleUISupport<V extends View> extends UISupportImpl<V> {
    public SimpleUISupport(Context context, Map<String, String> attr) {
        super(context, attr);
    }

    @Setter("default")
    public void setDefault(UIAttribute attr) {
        this.setDefaultValue(this.getView(), attr);
    }

    @Setter("text")
    public void setText(String text) {
        V view = this.getView();
        if (view instanceof TextView) {
            ((TextView) view).setText(text);
        }
    }


    @Setter("fontSize")
    public void setFontSize(float size) {
        V view = this.getView();
        if (view instanceof TextView) {
            ((TextView) view).setTextSize(size);
        }
    }

    @Setter("fontBold")
    public void setFontBold(boolean flag) {
        V view = this.getView();
        if (view instanceof TextView) {
            ((TextView) view).setTypeface(flag ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        }
    }

    @Setter("fontColor")
    public void setFontColor(String color) {
        V view = this.getView();
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(Color.parseColor(color));
        }
    }

    @Setter("paddingLeft")
    public void setPaddingLeft(int padding) {
        V view = this.getView();
        view.setPadding(padding, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
    }

    @Setter("paddingTop")
    public void setPaddingTop(int padding) {
        V view = this.getView();
        view.setPadding(view.getPaddingLeft(), padding, view.getPaddingRight(), view.getPaddingBottom());
    }

    @Setter("paddingRight")
    public void setPaddingRight(int padding) {
        V view = this.getView();
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), padding, view.getPaddingBottom());
    }

    @Setter("paddingBottom")
    public void setPaddingBottom(int padding) {
        V view = this.getView();
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), padding);
    }


    @Override
    protected boolean isContainer() {
        return false;
    }

    @Override
    protected abstract V initView(Context context);

    @Override
    protected void setValue(V view, UIValue value) {

    }

    protected void setDefaultValue(V view, UIAttribute value) {

    }

    @Override
    protected void bindViewValueChange(V v, UIValueSetter setter) {

    }
}
