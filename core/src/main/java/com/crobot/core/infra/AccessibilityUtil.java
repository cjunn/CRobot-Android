package com.crobot.core.infra;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

public class AccessibilityUtil {
    private static boolean isAccessibilityServiceEnabled(Activity context, Class clz) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo service : enabledServices) {
            if (service.getId().endsWith(clz.getSimpleName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean check(Activity context, Class clz, int requestCode) {
        if (isAccessibilityServiceEnabled(context, clz)) {
            return true;
        }
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivityForResult(intent, requestCode);
        return false;
    }


}
