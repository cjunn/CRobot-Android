package com.crobot.debug.file;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class FileHandle {
    private String root;

    public FileHandle(String root) {
        this.root = root;
    }

    private String normalUrl(String path) {
        return path.replace("\\", "/");
    }

    private String getString(DataInputStream dis) throws IOException {
        int pathLength = dis.readInt();
        byte[] pathBytes = new byte[pathLength];
        dis.readFully(pathBytes);
        String path = new String(pathBytes, "UTF-8");
        return normalUrl(path);
    }

    private int getInt(DataInputStream dis) throws IOException {
        return dis.readInt();
    }

    private String checkPath(String path) {
        File saveFile = new File(this.root, path);
        if (!saveFile.getParentFile().exists()) {
            saveFile.getParentFile().mkdirs();
        }
        return saveFile.getAbsolutePath();
    }

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {

            }
        }
    }

    public void invoke(SocketChannel clientChannel) throws IOException {
        DataInputStream dis = null;
        FileChannel fileChannel = null;
        try {
            dis = new DataInputStream(clientChannel.socket().getInputStream());
            String saveFilePath = checkPath(getString(dis));
            int totalLength = getInt(dis);
            fileChannel = new FileOutputStream(saveFilePath).getChannel();
            long totalTransferred = 0;
            long transferred;
            while (totalTransferred < totalLength) {
                long remaining = totalLength - totalTransferred;
                long transferSize = Math.min(1024 * 1024, remaining);
                transferred = fileChannel.transferFrom(clientChannel, totalTransferred, transferSize);
                if (transferred <= 0) {
                    throw new IOException("文件传输中断，已传输：" + totalTransferred + " 字节，预期：" + totalLength + " 字节");
                }
                totalTransferred += transferred;
            }
        } finally {
            close(dis);
            close(fileChannel);
            close(clientChannel);
        }
    }
}
