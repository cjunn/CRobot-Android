package com.crobot.core.infra;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.List;

public class SettingsCompat {
    private static final int OP_WRITE_SETTINGS = 23;
    private static final int OP_SYSTEM_ALERT_WINDOW = 24;
    private final static String HUAWEI_PACKAGE = "com.huawei.systemmanager";

    /**
     * 检查是否有权限
     *
     * @param context
     * @return
     */
    private static boolean hasDrawOverlays(Activity context) {
        return canDrawOverlays(context, false, false, -1);
    }

    public static boolean canDrawOverlays(Activity context, int requestCode) {
        if (hasDrawOverlays(context)) {
            return true;
        }
        return canDrawOverlays(context, true, true, requestCode);
    }

    /**
     * 检查悬浮窗权限  当没有权限，跳转到权限设置界面
     *
     * @param context          上下文
     * @param isShowDialog     没有权限，是否弹框提示跳转到权限设置界面
     * @param isShowPermission 是否跳转权限开启界面
     * @return true 有权限   false 没有权限（跳转权限界面、权限失败 提示用户手动设置权限）
     * @by 腾讯云直播 悬浮框判断逻辑
     */
    private static boolean canDrawOverlays(Activity context, boolean isShowDialog, boolean isShowPermission, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                if (isShowDialog) {
                    //去授权
                    SettingsCompat.manageDrawOverlays(context, requestCode);
                } else if (isShowPermission) {
                    SettingsCompat.manageDrawOverlays(context, requestCode);
                }
                return false;
            }
            return true;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (checkOp(context, OP_SYSTEM_ALERT_WINDOW)) {
                return true;
            } else {
                if (isShowPermission)
                    startFloatWindowPermissionErrorToast(context);
                return false;
            }
        } else {
            return true;
        }
    }

    private static void hookIntent(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 打开 悬浮窗 授权界面
     *
     * @param context
     */
    private static void manageDrawOverlays(Activity context, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                hookIntent(intent);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                context.startActivityForResult(intent, requestCode);
            } catch (Exception e) {
                e.printStackTrace();
                startFloatWindowPermissionErrorToast(context);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (!manageDrawOverlaysForRom(context, requestCode)) {
                startFloatWindowPermissionErrorToast(context);
            }
        }
    }

    /**
     * 权限设置 失败提示。
     *
     * @param context
     */
    private static void startFloatWindowPermissionErrorToast(Context context) {
        if (context != null)
            Toast.makeText(context, "进入设置页面失败,请手动开启悬浮窗权限", Toast.LENGTH_SHORT).show();
    }

    private static boolean manageDrawOverlaysForRom(Activity context, int requestCode) {
        if (RomUtil.isMiui()) {
            return manageDrawOverlaysForMiui(context, requestCode);
        }
        if (RomUtil.isEmui()) {
            return manageDrawOverlaysForEmui(context, requestCode);
        }
        if (RomUtil.isFlyme()) {
            return manageDrawOverlaysForFlyme(context, requestCode);
        }
        if (RomUtil.isOppo()) {
            return manageDrawOverlaysForOppo(context, requestCode);
        }
        if (RomUtil.isVivo()) {
            return manageDrawOverlaysForVivo(context, requestCode);
        }
        if (RomUtil.isQiku()) {
            return manageDrawOverlaysForQihu(context, requestCode);
        }
        if (RomUtil.isSmartisan()) {
            return manageDrawOverlaysForSmartisan(context, requestCode);
        }
        return false;
    }

    private static boolean checkOp(Context context, int op) {
        AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        try {
            Method method = AppOpsManager.class.getDeclaredMethod("checkOp", int.class, int.class, String.class);
            return AppOpsManager.MODE_ALLOWED == (int) method.invoke(manager, op, Binder.getCallingUid(), context.getPackageName());
        } catch (Exception e) {
        }
        return false;
    }

    // 可设置Android 4.3/4.4的授权状态
    private static boolean setMode(Context context, int op, boolean allowed) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2 || Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }

        AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        try {
            Method method = AppOpsManager.class.getDeclaredMethod("setMode", int.class, int.class, String.class, int.class);
            method.invoke(manager, op, Binder.getCallingUid(), context.getPackageName(), allowed ? AppOpsManager.MODE_ALLOWED : AppOpsManager.MODE_IGNORED);
            return true;
        } catch (Exception e) {

        }
        return false;
    }

    /**
     * 跳转界面
     *
     * @param context
     * @param intent
     * @return
     */
    private static boolean startSafely(Activity context, Intent intent, int requestCode) {
        List<ResolveInfo> resolveInfos = null;
        try {
            resolveInfos = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfos != null && resolveInfos.size() > 0) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivityForResult(intent, requestCode);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 小米
    private static boolean manageDrawOverlaysForMiui(Activity context, int requestCode) {
        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
        hookIntent(intent);
        intent.putExtra("extra_pkgname", context.getPackageName());
        intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
        if (startSafely(context, intent, requestCode)) {
            return true;
        }
        intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
        if (startSafely(context, intent, requestCode)) {
            return true;
        }
        // miui v5 的支持的android版本最高 4.x
        // http://www.romzj.com/list/search?keyword=MIUI%20V5#search_result
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Intent intent1 = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            hookIntent(intent1);
            intent1.setData(Uri.fromParts("package", context.getPackageName(), null));
            return startSafely(context, intent1, requestCode);
        }
        return false;
    }

    // 华为
    private static boolean manageDrawOverlaysForEmui(Activity context, int requestCode) {
        Intent intent = new Intent();
        hookIntent(intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.setClassName(HUAWEI_PACKAGE, "com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity");
            if (startSafely(context, intent, requestCode)) {
                return true;
            }
        }
        // Huawei Honor P6|4.4.4|3.0
        intent.setClassName(HUAWEI_PACKAGE, "com.huawei.notificationmanager.ui.NotificationManagmentActivity");
        intent.putExtra("showTabsNumber", 1);
        if (startSafely(context, intent, requestCode)) {
            return true;
        }
        intent.setClassName(HUAWEI_PACKAGE, "com.huawei.permissionmanager.ui.MainActivity");
        if (startSafely(context, intent, requestCode)) {
            return true;
        }
        return false;
    }

    // VIVO
    private static boolean manageDrawOverlaysForVivo(Activity context, int requestCode) {
        // 不支持直接到达悬浮窗设置页，只能到 i管家 首页
        Intent intent = new Intent("com.iqoo.secure");
        hookIntent(intent);
        intent.setClassName("com.iqoo.secure", "com.iqoo.secure.MainActivity");
        // com.iqoo.secure.ui.phoneoptimize.SoftwareManagerActivity
        // com.iqoo.secure.ui.phoneoptimize.FloatWindowManager
        return startSafely(context, intent, requestCode);
    }

    // OPPO
    private static boolean manageDrawOverlaysForOppo(Activity context, int requestCode) {
        Intent intent = new Intent();
        hookIntent(intent);
        intent.putExtra("packageName", context.getPackageName());
        // OPPO A53|5.1.1|2.1
        intent.setAction("com.oppo.safe");
        intent.setClassName("com.oppo.safe", "com.oppo.safe.permission.floatwindow.FloatWindowListActivity");
        if (startSafely(context, intent, requestCode)) {
            return true;
        }
        // OPPO R7s|4.4.4|2.1
        intent.setAction("com.ext.color.safecenter");
        intent.setClassName("com.ext.color.safecenter", "com.ext.color.safecenter.permission.floatwindow.FloatWindowListActivity");
        if (startSafely(context, intent, requestCode)) {
            return true;
        }
        intent.setAction("com.coloros.safecenter");
        intent.setClassName("com.coloros.safecenter", "com.coloros.safecenter.sysfloatwindow.FloatWindowListActivity");
        return startSafely(context, intent, requestCode);
    }

    // 魅族
    private static boolean manageDrawOverlaysForFlyme(Activity context, int requestCode) {
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        hookIntent(intent);
        intent.setClassName("com.meizu.safe", "com.meizu.safe.security.AppSecActivity");
        intent.putExtra("packageName", context.getPackageName());
        return startSafely(context, intent, requestCode);
    }

    // 360
    private static boolean manageDrawOverlaysForQihu(Activity context, int requestCode) {
        Intent intent = new Intent();
        hookIntent(intent);
        intent.setClassName("com.android.settings", "com.android.settings.Settings$OverlaySettingsActivity");
        if (startSafely(context, intent, requestCode)) {
            return true;
        }
        intent.setClassName("com.qihoo360.mobilesafe", "com.qihoo360.mobilesafe.ui.index.AppEnterActivity");
        return startSafely(context, intent, requestCode);
    }

    // 锤子
    private static boolean manageDrawOverlaysForSmartisan(Activity context, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 锤子 坚果|5.1.1|2.5.3
            Intent intent = new Intent("com.smartisanos.security.action.SWITCHED_PERMISSIONS_NEW");
            hookIntent(intent);
            intent.setClassName("com.smartisanos.security", "com.smartisanos.security.SwitchedPermissions");
            intent.putExtra("index", 17); // 不同版本会不一样
            return startSafely(context, intent, requestCode);
        } else {
            // 锤子 坚果|4.4.4|2.1.2
            Intent intent = new Intent("com.smartisanos.security.action.SWITCHED_PERMISSIONS");
            hookIntent(intent);
            intent.setClassName("com.smartisanos.security", "com.smartisanos.security.SwitchedPermissions");
            intent.putExtra("permission", new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW});
            return startSafely(context, intent, requestCode);
        }
    }
}
