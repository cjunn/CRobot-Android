package com.crobot.core.infra.tool;

import java.io.InputStream;
import java.util.List;

public interface FileOperation {
    byte[] read(String path);

    String readText(String path);

    boolean exits(String path);

    void write(String path, byte[] body);

    void write(String path, InputStream is, int length);

    void writeText(String path, String body);

    String pwd();

    FileOperation open(String child);

    boolean isFile(String path);

    boolean isDirectory(String path);

    boolean remove(String path);

    String md5(String path);

    List<String> listFiles();
}
