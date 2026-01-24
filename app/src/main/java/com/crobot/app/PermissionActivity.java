package com.crobot.app;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;

import com.crobot.core.infra.CommonPermissionActivity;

public class PermissionActivity extends CommonPermissionActivity {

    @Override
    protected Class<? extends Activity> startNextActivity() {
        return AppActivity.class;
    }

    @Override
    protected Class<? extends AccessibilityService> accessibilityService() {
        return AppAccessibility.class;
    }


}
