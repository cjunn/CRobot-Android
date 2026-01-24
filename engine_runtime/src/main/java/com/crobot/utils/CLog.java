package com.crobot.utils;

import android.util.Log;

public class CLog {
    private final static String TAG = "C_START_ENGINE";

    public static void debug(String text) {
        Log.d(TAG, text);
    }

    public static void error(String msg, Throwable thr) {
        Log.e(TAG, msg, thr);
    }

    public static void error(String msg) {
        Log.e(TAG, msg);
    }

    public static void info(String text) {
        text = text == null ? "" : text;
        Log.i(TAG, text);
    }

    public static void info(String tag, String text) {
        Log.i(TAG, "[" + tag + "]" + text);
    }
}
