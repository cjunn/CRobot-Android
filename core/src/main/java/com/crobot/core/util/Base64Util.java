package com.crobot.core.util;

public class Base64Util {
    public static String encode(byte[] data) {
        return android.util.Base64.encodeToString(data, android.util.Base64.NO_WRAP);
    }

    public static byte[] decode(String base64) {
        return android.util.Base64.decode(base64, android.util.Base64.NO_WRAP);
    }

}
