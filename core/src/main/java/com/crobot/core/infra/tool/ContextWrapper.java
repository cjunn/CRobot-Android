package com.crobot.core.infra.tool;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.view.Display;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ContextWrapper extends Context {
    private final Context context;

    public ContextWrapper(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.context = context;
    }

    @Override
    public AssetManager getAssets() {
        return this.context.getAssets();
    }

    @Override
    public Resources getResources() {
        return this.context.getResources();
    }

    @Override
    public PackageManager getPackageManager() {
        return this.context.getPackageManager();
    }

    @Override
    public ContentResolver getContentResolver() {
        return this.context.getContentResolver();
    }

    @Override
    public Looper getMainLooper() {
        return this.context.getMainLooper();
    }

    @Override
    public Context getApplicationContext() {
        return this.context.getApplicationContext();
    }

    @Override
    public void setTheme(int resid) {
        this.context.setTheme(resid);
    }

    @Override
    public Resources.Theme getTheme() {
        return this.context.getTheme();
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.context.getClassLoader();
    }

    @Override
    public String getPackageName() {
        return this.context.getPackageName();
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return this.context.getApplicationInfo();
    }

    @Override
    public String getPackageResourcePath() {
        return this.context.getPackageResourcePath();
    }

    @Override
    public String getPackageCodePath() {
        return this.context.getPackageCodePath();
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return this.context.getSharedPreferences(name, mode);
    }

    @Override
    public boolean moveSharedPreferencesFrom(Context sourceContext, String name) {
        return this.context.moveSharedPreferencesFrom(sourceContext, name);
    }

    @Override
    public boolean deleteSharedPreferences(String name) {
        return this.context.deleteSharedPreferences(name);
    }

    @Override
    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        return this.context.openFileInput(name);
    }

    @Override
    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        return this.context.openFileOutput(name, mode);
    }

    @Override
    public boolean deleteFile(String name) {
        return this.context.deleteFile(name);
    }

    @Override
    public File getFileStreamPath(String name) {
        return this.context.getFileStreamPath(name);
    }

    @Override
    public File getDataDir() {
        return this.context.getDataDir();
    }

    @Override
    public File getFilesDir() {
        return this.context.getFilesDir();
    }

    @Override
    public File getNoBackupFilesDir() {
        return this.context.getNoBackupFilesDir();
    }

    @Nullable
    @Override
    public File getExternalFilesDir(@Nullable String type) {
        return this.context.getExternalFilesDir(type);
    }

    @Override
    public File[] getExternalFilesDirs(String type) {
        return this.context.getExternalFilesDirs(type);
    }

    @Override
    public File getObbDir() {
        return this.context.getObbDir();
    }

    @Override
    public File[] getObbDirs() {
        return this.context.getObbDirs();
    }

    @Override
    public File getCacheDir() {
        return this.context.getCacheDir();
    }

    @Override
    public File getCodeCacheDir() {
        return this.context.getCodeCacheDir();
    }

    @Nullable
    @Override
    public File getExternalCacheDir() {
        return this.context.getExternalCacheDir();
    }

    @Override
    public File[] getExternalCacheDirs() {
        return this.context.getExternalCacheDirs();
    }

    @Override
    public File[] getExternalMediaDirs() {
        return this.context.getExternalMediaDirs();
    }

    @Override
    public String[] fileList() {
        return this.context.fileList();
    }

    @Override
    public File getDir(String name, int mode) {
        return this.context.getDir(name, mode);
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        return this.context.openOrCreateDatabase(name, mode, factory);
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, @Nullable DatabaseErrorHandler errorHandler) {
        return this.context.openOrCreateDatabase(name, mode, factory, errorHandler);
    }

    @Override
    public boolean moveDatabaseFrom(Context sourceContext, String name) {
        return this.context.moveDatabaseFrom(sourceContext, name);
    }

    @Override
    public boolean deleteDatabase(String name) {
        return this.context.deleteDatabase(name);
    }

    @Override
    public File getDatabasePath(String name) {
        return this.context.getDatabasePath(name);
    }

    @Override
    public String[] databaseList() {
        return this.context.databaseList();
    }

    @Override
    public Drawable getWallpaper() {
        return this.context.getWallpaper();
    }

    @Override
    public Drawable peekWallpaper() {
        return this.context.peekWallpaper();
    }

    @Override
    public int getWallpaperDesiredMinimumWidth() {
        return this.context.getWallpaperDesiredMinimumWidth();
    }

    @Override
    public int getWallpaperDesiredMinimumHeight() {
        return this.context.getWallpaperDesiredMinimumHeight();
    }

    @Override
    public void setWallpaper(Bitmap bitmap) throws IOException {
        this.context.setWallpaper(bitmap);
    }

    @Override
    public void setWallpaper(InputStream data) throws IOException {
        this.context.setWallpaper(data);
    }

    @Override
    public void clearWallpaper() throws IOException {
        this.context.clearWallpaper();
    }

    @Override
    public void startActivity(Intent intent) {
        this.context.startActivity(intent);
    }

    @Override
    public void startActivity(Intent intent, @Nullable Bundle options) {
        this.context.startActivity(intent, options);
    }

    @Override
    public void startActivities(Intent[] intents) {
        this.context.startActivities(intents);
    }

    @Override
    public void startActivities(Intent[] intents, Bundle options) {
        this.context.startActivities(intents, options);
    }

    @Override
    public void startIntentSender(IntentSender intent, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws IntentSender.SendIntentException {
        this.context.startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags);
    }

    @Override
    public void startIntentSender(IntentSender intent, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, @Nullable Bundle options) throws IntentSender.SendIntentException {
        this.context.startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags, options);
    }

    @Override
    public void sendBroadcast(Intent intent) {
        this.context.sendBroadcast(intent);
    }

    @Override
    public void sendBroadcast(Intent intent, @Nullable String receiverPermission) {
        this.context.sendBroadcast(intent, receiverPermission);
    }

    @Override
    public void sendOrderedBroadcast(Intent intent, @Nullable String receiverPermission) {
        this.context.sendOrderedBroadcast(intent, receiverPermission);
    }

    @Override
    public void sendOrderedBroadcast(@NonNull Intent intent, @Nullable String receiverPermission, @Nullable BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {
        this.context.sendOrderedBroadcast(intent, receiverPermission, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    @RequiresPermission("android.permission.INTERACT_ACROSS_USERS")
    @Override
    public void sendBroadcastAsUser(Intent intent, UserHandle user) {
        this.context.sendBroadcastAsUser(intent, user);
    }

    @RequiresPermission("android.permission.INTERACT_ACROSS_USERS")
    @Override
    public void sendBroadcastAsUser(Intent intent, UserHandle user, @Nullable String receiverPermission) {
        this.context.sendBroadcastAsUser(intent, user, receiverPermission);
    }

    @RequiresPermission("android.permission.INTERACT_ACROSS_USERS")
    @Override
    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, @Nullable String receiverPermission, BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {
        this.context.sendOrderedBroadcastAsUser(intent, user, receiverPermission, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    @RequiresPermission(Manifest.permission.BROADCAST_STICKY)
    @Override
    public void sendStickyBroadcast(Intent intent) {
        this.context.sendStickyBroadcast(intent);
    }

    @RequiresPermission(Manifest.permission.BROADCAST_STICKY)
    @Override
    public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {
        this.context.sendStickyOrderedBroadcast(intent, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    @RequiresPermission(Manifest.permission.BROADCAST_STICKY)
    @Override
    public void removeStickyBroadcast(Intent intent) {
        this.context.removeStickyBroadcast(intent);
    }

    @RequiresPermission(allOf = {Manifest.permission.BROADCAST_STICKY, "android.permission.INTERACT_ACROSS_USERS"})
    @Override
    public void sendStickyBroadcastAsUser(Intent intent, UserHandle user) {
        this.context.sendStickyBroadcastAsUser(intent, user);
    }

    @RequiresPermission(allOf = {Manifest.permission.BROADCAST_STICKY, "android.permission.INTERACT_ACROSS_USERS"})
    @Override
    public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle user, BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {
        this.context.sendStickyOrderedBroadcastAsUser(intent, user, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    @RequiresPermission(allOf = {Manifest.permission.BROADCAST_STICKY, "android.permission.INTERACT_ACROSS_USERS"})
    @Override
    public void removeStickyBroadcastAsUser(Intent intent, UserHandle user) {
        this.context.removeStickyBroadcastAsUser(intent, user);
    }

    @Nullable
    @Override
    public Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter) {
        return this.context.registerReceiver(receiver, filter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter, int flags) {
        return this.context.registerReceiver(receiver, filter, flags);
    }

    @Nullable
    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, @Nullable String broadcastPermission, @Nullable Handler scheduler) {
        return this.context.registerReceiver(receiver, filter, broadcastPermission, scheduler);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, @Nullable String broadcastPermission, @Nullable Handler scheduler, int flags) {
        return this.context.registerReceiver(receiver, filter, broadcastPermission, scheduler, flags);
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        this.context.unregisterReceiver(receiver);
    }

    @Nullable
    @Override
    public ComponentName startService(Intent service) {
        return this.context.startService(service);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public ComponentName startForegroundService(Intent service) {
        return this.context.startForegroundService(service);
    }

    @Override
    public boolean stopService(Intent service) {
        return this.context.stopService(service);
    }

    @Override
    public boolean bindService(Intent service, @NonNull ServiceConnection conn, int flags) {
        return this.context.bindService(service, conn, flags);
    }

    @Override
    public void unbindService(@NonNull ServiceConnection conn) {
        this.context.unbindService(conn);
    }

    @Override
    public boolean startInstrumentation(@NonNull ComponentName className, @Nullable String profileFile, @Nullable Bundle arguments) {
        return this.context.startInstrumentation(className, profileFile, arguments);
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        return this.context.getSystemService(name);
    }

    @Nullable
    @Override
    public String getSystemServiceName(@NonNull Class<?> serviceClass) {
        return this.context.getSystemServiceName(serviceClass);
    }

    @Override
    public int checkPermission(@NonNull String permission, int pid, int uid) {
        return this.context.checkPermission(permission, pid, uid);
    }

    @Override
    public int checkCallingPermission(@NonNull String permission) {
        return this.context.checkCallingPermission(permission);
    }

    @Override
    public int checkCallingOrSelfPermission(@NonNull String permission) {
        return this.context.checkCallingOrSelfPermission(permission);
    }

    @Override
    public int checkSelfPermission(@NonNull String permission) {
        return this.context.checkSelfPermission(permission);
    }

    @Override
    public void enforcePermission(@NonNull String permission, int pid, int uid, @Nullable String message) {
        this.context.enforcePermission(permission, pid, uid, message);
    }

    @Override
    public void enforceCallingPermission(@NonNull String permission, @Nullable String message) {
        this.context.enforceCallingPermission(permission, message);
    }

    @Override
    public void enforceCallingOrSelfPermission(@NonNull String permission, @Nullable String message) {
        this.context.enforceCallingOrSelfPermission(permission, message);
    }

    @Override
    public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {
        this.context.grantUriPermission(toPackage, uri, modeFlags);
    }

    @Override
    public void revokeUriPermission(Uri uri, int modeFlags) {
        this.context.revokeUriPermission(uri, modeFlags);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void revokeUriPermission(String toPackage, Uri uri, int modeFlags) {
        this.context.revokeUriPermission(toPackage, uri, modeFlags);
    }

    @Override
    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
        return this.context.checkUriPermission(uri, pid, uid, modeFlags);
    }

    @Override
    public int checkCallingUriPermission(Uri uri, int modeFlags) {
        return this.context.checkCallingUriPermission(uri, modeFlags);
    }

    @Override
    public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
        return this.context.checkCallingOrSelfUriPermission(uri, modeFlags);
    }

    @Override
    public int checkUriPermission(@Nullable Uri uri, @Nullable String readPermission, @Nullable String writePermission, int pid, int uid, int modeFlags) {
        return this.context.checkUriPermission(uri, readPermission, writePermission, pid, uid, modeFlags);
    }

    @Override
    public void enforceUriPermission(Uri uri, int pid, int uid, int modeFlags, String message) {
        this.context.enforceUriPermission(uri, pid, uid, modeFlags, message);
    }

    @Override
    public void enforceCallingUriPermission(Uri uri, int modeFlags, String message) {
        this.context.enforceCallingUriPermission(uri, modeFlags, message);
    }

    @Override
    public void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags, String message) {
        this.context.enforceCallingOrSelfUriPermission(uri, modeFlags, message);
    }

    @Override
    public void enforceUriPermission(@Nullable Uri uri, @Nullable String readPermission, @Nullable String writePermission, int pid, int uid, int modeFlags, @Nullable String message) {
        this.context.enforceUriPermission(uri, readPermission, writePermission, pid, uid, modeFlags, message);
    }

    @Override
    public Context createPackageContext(String packageName, int flags) throws PackageManager.NameNotFoundException {
        return this.context.createPackageContext(packageName, flags);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public Context createContextForSplit(String splitName) throws PackageManager.NameNotFoundException {
        return this.context.createContextForSplit(splitName);
    }

    @Override
    public Context createConfigurationContext(@NonNull Configuration overrideConfiguration) {
        return this.context.createConfigurationContext(overrideConfiguration);
    }

    @Override
    public Context createDisplayContext(@NonNull Display display) {
        return this.context.createDisplayContext(display);
    }

    @Override
    public Context createDeviceProtectedStorageContext() {
        return this.context.createDeviceProtectedStorageContext();
    }

    @Override
    public boolean isDeviceProtectedStorage() {
        return this.context.isDeviceProtectedStorage();
    }
}