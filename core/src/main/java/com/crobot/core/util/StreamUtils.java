package com.crobot.core.util;

import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtils {
    public static final int DEFAULT_BUFFER_SIZE = 8192;

    public static void copy(InputStream in, OutputStream out) {
        if (in == null) {
            return;
        }
        if (out == null) {
            return;
        }
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int bytesRead;
        try {
            InputStream inputStream = in;
            OutputStream outputStream = out;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        } catch (Exception e) {
            throw new RuntimeException("流复制失败", e);
        }
    }
}
