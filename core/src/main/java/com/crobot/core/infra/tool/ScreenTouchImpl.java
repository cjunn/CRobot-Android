package com.crobot.core.infra.tool;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;

public class ScreenTouchImpl implements ScreenTouch {
    private AccessibilityService accessibility;

    public ScreenTouchImpl(AccessibilityService accessibility) {
        this.accessibility = accessibility;
    }

    @Override
    public boolean tap(float x, float y, int delay) {
        if (x < 0 || y < 0) {
            return false;
        }
        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y);
        builder.addStroke(new GestureDescription.StrokeDescription(path, 1, delay));
        final GestureDescription build = builder.build();
        return this.accessibility.dispatchGesture(build, null, null);
    }


    @Override
    public boolean swipe(float x1, float y1, float x2, float y2, int duration) {
        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path path = new Path();
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        builder.addStroke(new GestureDescription.StrokeDescription(path, 1, (long) duration));
        return this.accessibility.dispatchGesture(builder.build(), null, null);
    }

}
