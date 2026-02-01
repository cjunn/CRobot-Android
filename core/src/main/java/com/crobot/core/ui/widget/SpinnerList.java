package com.crobot.core.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.widget.AppCompatSpinner;

import com.crobot.core.resource.My;
import com.crobot.core.ui.core.SimpleUISupport;
import com.crobot.core.ui.core.UIAttribute;
import com.crobot.core.ui.core.UISupport;
import com.crobot.core.ui.core.UIValueSetter;

import java.util.Map;

public class SpinnerList extends SimpleUISupport<AppCompatSpinner, String> {
    private ArrayAdapter<KeyValue> adapter;

    public SpinnerList(Context context, Map<String, String> attr) {
        super(context, attr);
    }

    @Override
    protected boolean isContainer() {
        return true;
    }

    @SuppressLint("ResourceType")
    @Override
    protected AppCompatSpinner initView(Context context) {
        this.adapter = new ArrayAdapter<>(context, My.style.spinner_item);
        this.adapter.setDropDownViewResource(My.style.spinner_dropdown_item);
        AppCompatSpinner spinner = new AppCompatSpinner(context);
        spinner.setBackgroundResource(My.style.spinner_layer);
        spinner.setPadding(2, 0, 2, 0);
        spinner.setAdapter(adapter);
        return spinner;
    }

    @Override
    protected void setValue(AppCompatSpinner spinner, String value) {
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            if (value.equals(adapter.getItem(i).getKey())) {
                spinner.setSelection(i);
                spinner.invalidate();
                break;
            }
        }
    }

    @Override
    protected void setDefaultValue(AppCompatSpinner spinner, UIAttribute value) {
        this.setValue(spinner, value.getString());
    }

    @Override
    protected void bindViewValueChange(AppCompatSpinner spinner, UIValueSetter setter) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String key = adapter.getItem(position).getKey();
                setter.apply(key);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setter.apply("");
            }
        });
    }

    @Override
    public <K1 extends View, E> void addChild(UISupport<K1, E> c) {
        super.addChild(c);
        if (c instanceof SpinnerEntry) {
            addChild((SpinnerEntry) c);
        }
    }

    private void addChild(SpinnerEntry c) {
        adapter.add(new KeyValue(c));
        adapter.notifyDataSetChanged();
        c.setObserver(() -> adapter.notifyDataSetChanged());
    }

    public static class KeyValue {
        private SpinnerEntry spinnerEntry;

        public KeyValue(SpinnerEntry spinnerEntry) {
            this.spinnerEntry = spinnerEntry;
        }

        public String getKey() {
            return this.spinnerEntry.getKey();
        }

        public String getText() {
            return this.spinnerEntry.getText();
        }

        public String toString() {
            String value = this.getText();
            return value == null ? "" : value;
        }
    }
}
