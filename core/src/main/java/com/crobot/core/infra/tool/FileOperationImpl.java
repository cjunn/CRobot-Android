package com.crobot.core.infra.tool;

import com.crobot.core.util.FileUtil;
import com.crobot.core.util.MD5Util;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileOperationImpl implements FileOperation {
    private String root;

    public FileOperationImpl(String root) {
        this.root = root;
    }

    public static boolean deleteAll(String path) {
        File rootDir = new File(path);
        if (!rootDir.exists()) {
            return false;
        }
        if (!rootDir.isDirectory()) {
            rootDir.delete();
            return true;
        }
        File[] allItems = rootDir.listFiles();
        if (allItems != null) {
            for (File item : allItems) {
                if (item.isFile()) {
                    item.delete();
                } else if (item.isDirectory()) {
                    deleteAll(item.getAbsolutePath());
                }
            }
        }
        rootDir.delete();
        return true;
    }

    @Override
    public synchronized byte[] read(String path) {
        return FileUtil.readFileByte(new File(root, path));
    }

    @Override
    public synchronized String readText(String path) {
        return new String(this.read(path), StandardCharsets.UTF_8);
    }

    @Override
    public synchronized boolean exits(String path) {
        return new File(root, path).exists();
    }

    @Override
    public synchronized void write(String path, byte[] body) {
        FileUtil.writeFileByte(new File(root, path), body);
    }

    @Override
    public void write(String path, InputStream is, int length) {
        FileUtil.writeFileStream(new File(root, path), is, length);
    }

    @Override
    public synchronized void writeText(String path, String body) {
        write(path, body.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String pwd() {
        return root;
    }

    @Override
    public FileOperation open(String child) {
        return new FileOperationImpl(new File(root, child).getAbsolutePath());
    }

    @Override
    public boolean isFile(String path) {
        return new File(root, path).isFile();
    }

    @Override
    public boolean isDirectory(String path) {
        return new File(root, path).isDirectory();
    }

    @Override
    public boolean remove(String path) {
        return deleteAll(new File(root, path).getAbsolutePath());
    }

    @Override
    public String md5(String path) {
        return MD5Util.calculateFileMD5(new File(root, path).getAbsolutePath());
    }


    public List<String> listAllFile(File file, String root, List<String> all) {
        File[] items = file.listFiles();
        if (items == null) {
            return all;
        }
        for (File item : items) {
            if (item.isFile()) {
                String absolutePath = item.getAbsolutePath();
                all.add(absolutePath.substring(root.length()));
            } else {
                listAllFile(item, root, all);
            }
        }
        return all;
    }


    @Override
    public List<String> listFiles() {
        File file = new File(root);
        return listAllFile(file, file.getAbsolutePath(), new ArrayList<>());
    }
}
