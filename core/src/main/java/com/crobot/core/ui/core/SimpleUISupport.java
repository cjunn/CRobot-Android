package com.crobot.core.ui.core;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import com.crobot.utils.CLog;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

public abstract class SimpleUISupport<V extends View, E> extends UISupportImpl<V, E> {
    public SimpleUISupport(Context context, Map<String, String> attr) {
        super(context, attr);
    }

    @Setter("default")
    public void setDefault(String value) {
        this.setValue(DefaultParse.toValue(this.getClass(), value));
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
    protected void setValue(V view, E value) {

    }


    @Override
    protected void bindViewValueChange(V v, UIValueSetter<E> setter) {

    }

    public static class DefaultParse {
        public static <E> E toValue(Class clz, String value) {
            Type valueType = getValueType(clz);
            if (Integer.class.equals(valueType)) {
                return (E) toInteger(value);
            }
            if (String.class.equals(valueType)) {
                return (E) toString(value);
            }
            if (Boolean.class.equals(valueType)) {
                return (E) toBool(value);
            }
            return null;
        }

        public static Integer toInteger(String value) {
            try {
                return Integer.parseInt(value);
            } catch (Exception e) {
                return 0;
            }
        }

        public static String toString(String value) {
            return value;
        }

        public static Boolean toBool(String value) {
            try {
                return Boolean.parseBoolean(value);
            } catch (Exception e) {
                return false;
            }
        }


        public static Type getValueType(Class clz) {
            Type genericSuperclass = clz.getGenericSuperclass();
            ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            return actualTypeArguments[1];
        }
    }

}
