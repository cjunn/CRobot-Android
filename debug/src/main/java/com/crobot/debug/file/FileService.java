package com.crobot.debug.file;

import com.crobot.utils.CLog;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class FileService {
    private int port;
    private ServerSocketChannel serverChannel;

    public FileService(int port) {
        this.port = port;
    }

    public synchronized void start(String root) {
        try {
            this.serverChannel = ServerSocketChannel.open();
            this.serverChannel.socket().bind(new InetSocketAddress(this.port));
            this.serverChannel.configureBlocking(true);
            new Thread(() -> {
                try {
                    this.run(this.serverChannel, root);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    close();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void run(ServerSocketChannel server, String root) throws IOException {
        Thread thread = Thread.currentThread();
        while (!thread.isInterrupted()) {
            if (server == null) {
                return;
            }
            SocketChannel socketChannel = null;
            try {
                new FileHandle(root).invoke(server.accept());
            } catch (Exception e) {
                CLog.error("Socket连接出现异常1", e);
            } finally {
                if (socketChannel != null) {
                    socketChannel.close();
                }
            }
        }
    }

    public synchronized void close() {
        if (this.serverChannel == null) {
            return;
        }
        try {
            this.serverChannel.close();
            this.serverChannel = null;
        } catch (IOException e) {
        }
    }
}
