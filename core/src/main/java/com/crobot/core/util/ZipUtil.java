package com.crobot.core.util;

import android.util.Pair;

import com.crobot.utils.CLog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtil {
    public static void extract(byte[] data, Consumer<Pair<String, byte[]>> consumer) {
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(data))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    zipInputStream.closeEntry();
                    continue;
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                StreamUtils.copy(zipInputStream, baos);
                zipInputStream.closeEntry();
                consumer.accept(new Pair<>(entry.getName(), baos.toByteArray()));
            }
        } catch (IOException e) {
            CLog.error("ZipUtil", e);
        }
    }
}
