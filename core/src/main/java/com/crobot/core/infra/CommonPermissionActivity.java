package com.crobot.core.infra;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.crobot.core.resource.My;

import java.util.LinkedList;
import java.util.function.Consumer;

public abstract class CommonPermissionActivity extends Activity {
    private static String[] PERMISSIONS_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private static int STORAGE_REQUEST_CODE = 10001;
    private static int DRAW_OVERLAYS_CODE = 10002;
    private static int CAPTURE_CODE = 10003;
    private static int ACCESSIBILITY_CODE = 10004;
    private LinkedList<Grant> grants = new LinkedList<>();
    private Intent nextIntent;

    public CommonPermissionActivity() {
        CommonPermissionActivity thiz = this;
        grants.add(new Grant(STORAGE_REQUEST_CODE, () -> {
            if (this.checkStorage()) {
                thiz.onActivityResult(STORAGE_REQUEST_CODE, RESULT_OK, null);
                return;
            }
        }));
        grants.add(new Grant(DRAW_OVERLAYS_CODE, () -> {
            if (checkOverlays()) {
                thiz.onActivityResult(DRAW_OVERLAYS_CODE, RESULT_OK, null);
                return;
            }
            Toast.makeText(this.getApplicationContext(), "请手动开启获取悬浮窗权限！", Toast.LENGTH_LONG).show();
        }));
        grants.add(new Grant(CAPTURE_CODE, () -> {
            MediaProjectionManager media = (MediaProjectionManager) thiz.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            thiz.startActivityForResult(media.createScreenCaptureIntent(), CAPTURE_CODE);
        }, (Consumer<Intent>) intent -> {
            nextIntent.putExtra(InfraConst.ScreenCapture, intent);
        }));
        grants.add(new Grant(ACCESSIBILITY_CODE, () -> {
            if (AccessibilityUtil.check(this, accessibilityService(), ACCESSIBILITY_CODE)) {
                thiz.onActivityResult(ACCESSIBILITY_CODE, RESULT_OK, null);
                return;
            }
            Toast.makeText(this, "请手动开启屏幕无障碍权限！", Toast.LENGTH_LONG).show();
        }));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(My.layout.permission);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.nextIntent = new Intent(this, startNextActivity());
        this.runOnUiThread(() -> grant(grants.get(0).requestCode, false, null));
    }

    private boolean checkStorage() {
        if (ActivityCompat.checkSelfPermission(this, PERMISSIONS_STORAGE[0]) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, STORAGE_REQUEST_CODE);
        return false;
    }

    private boolean checkOverlays() {
        return SettingsCompat.canDrawOverlays(this, DRAW_OVERLAYS_CODE);
    }

    protected abstract Class<? extends Activity> startNextActivity();

    protected abstract Class<? extends AccessibilityService> accessibilityService();

    private void completeGrant() {
        startActivity(nextIntent);
        this.finish();
    }


    public void grant(int requestCode, boolean success, Intent intent) {
        if (grants.isEmpty()) {
            completeGrant();
            return;
        }
        if (grants.get(0).requestCode != requestCode) {
            return;
        }
        if (success) {
            Grant grant = grants.removeFirst();
            if (grant.complete != null) {
                grant.complete.accept(intent);
            }
            if (grants.isEmpty()) {
                completeGrant();
                return;
            }
        }
        grants.getFirst().runnable.run();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        grant(requestCode, grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        grant(requestCode, resultCode == RESULT_OK, data);
    }

    private static class Grant<T> {
        protected final int requestCode;
        protected final Runnable runnable;
        protected final Consumer<T> complete;

        public Grant(int requestCode, Runnable runnable) {
            this.requestCode = requestCode;
            this.runnable = runnable;
            this.complete = null;
        }

        public Grant(int requestCode, Runnable runnable, Consumer<T> complete) {
            this.requestCode = requestCode;
            this.runnable = runnable;
            this.complete = complete;
        }

    }


}
