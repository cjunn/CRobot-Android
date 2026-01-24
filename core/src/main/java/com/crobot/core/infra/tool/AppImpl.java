package com.crobot.core.infra.tool;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.core.content.FileProvider;

import com.crobot.core.infra.AccessibilityComponent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppImpl implements App {
    private Context context;

    public AppImpl(Context context) {
        this.context = context;
    }

    @Override
    public String archite() {
        String[] abis = Build.SUPPORTED_ABIS;
        if (abis != null && abis.length > 0) {
            return abis[0]; // 第一个是设备首选的主架构
        }
        return null;
    }

    @Override
    public String currentPackage() {
        AccessibilityService service = AccessibilityComponent.get();
        if (service == null) {
            return null;
        }
        AccessibilityNodeInfo node = service.getRootInActiveWindow();
        if (node == null) {
            return null;
        }
        CharSequence packageName = node.getPackageName();
        if (packageName == null) {
            return null;
        }
        return packageName.toString();
    }

    @Override
    public boolean launch(String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            context.startActivity(packageManager.getLaunchIntentForPackage(packageName)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void uninstall(String packageName) {
        context.startActivity(new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + packageName))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    public boolean viewFile(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                return false;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = getFileUri(file);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && uri.getScheme().equals("content")) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            String mimeType = getMimeType(path);
            intent.setDataAndType(uri, mimeType);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean editFile(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                return false;
            }
            Intent intent = new Intent(Intent.ACTION_EDIT);
            Uri uri = getFileUri(file);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && uri.getScheme().equals("content")) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            String mimeType = getMimeType(path);
            intent.setDataAndType(uri, mimeType);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean openUrl(String url) {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void getInstalledApps() {
        List<Map<String, String>> apps = new ArrayList<>();
        try {
            PackageManager pm = context.getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            for (ApplicationInfo packageInfo : packages) {
                Map<String, String> appInfo = new HashMap<>();
                appInfo.put("packageName", packageInfo.packageName);
                appInfo.put("name", pm.getApplicationLabel(packageInfo).toString());
                apps.add(appInfo);
            }
        } catch (Exception e) {
            // Return empty list on error
        }
    }

    private Uri getFileUri(File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
            } catch (Exception e) {
                // If FileProvider is not configured, fall back to file:// URI
                // Note: This may not work on Android N+ without proper permissions
                return Uri.fromFile(file);
            }
        } else {
            return Uri.fromFile(file);
        }
    }

    private String getMimeType(String path) {
        String extension = "";
        int lastDot = path.lastIndexOf('.');
        if (lastDot > 0) {
            extension = path.substring(lastDot + 1).toLowerCase();
        }
        switch (extension) {
            case "txt":
            case "log":
                return "text/plain";
            case "pdf":
                return "application/pdf";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "mp4":
                return "video/mp4";
            case "mp3":
                return "audio/mpeg";
            case "html":
            case "htm":
                return "text/html";
            case "xml":
                return "text/xml";
            case "json":
                return "application/json";
            default:
                return "*/*";
        }
    }


}
