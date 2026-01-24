package com.crobot.core.infra;

import android.accessibilityservice.AccessibilityService;

public class AccessibilityComponent {
    private static AccessibilityService instance;

    protected static void register(AccessibilityService service) {
        instance = service;
    }

    public static AccessibilityService get() {
//        if(instance==null){
//            throw new InfraError("暂未完成初始化!");
//        }
        return instance;
    }
}
