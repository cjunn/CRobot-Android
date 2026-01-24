package com.crobot.debug;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.crobot.core.infra.CommonPermissionActivity;


public class PermissionActivity extends CommonPermissionActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Class<? extends Activity> startNextActivity() {
        return DebuggerActivity.class;
    }

    @Override
    protected Class<? extends AccessibilityService> accessibilityService() {
        return DebuggerAccessibility.class;
    }


}
