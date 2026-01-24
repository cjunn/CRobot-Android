package com.crobot.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class MD5Util {
    private static final int BUFFER_SIZE = 64 * 1024;

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public static String calculateFileMD5(String filePath) {
        try {
            if (!new File(filePath).isFile()) {
                return null;
            }
            try (InputStream fis = new FileInputStream(filePath)) {
                return calculateMD5(fis);
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static String calculateMD5(InputStream inputStream) {
        try {
            MessageDigest md5Digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                md5Digest.update(buffer, 0, bytesRead);
            }
            byte[] md5Bytes = md5Digest.digest();
            return bytesToHex(md5Bytes);
        } catch (Exception e) {
            return null;
        }

    }
}
