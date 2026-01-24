package com.crobot.debug.scan;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class ScanService {
    private static final int BUFFER_SIZE = 1024;
    private static String CROBOT = "CROBOT";
    private int port;
    private DatagramSocket socket;

    public ScanService(int port) {
        this.port = port;
    }

    private void run(DatagramSocket socket) throws IOException {
        byte[] receiveBuffer = new byte[BUFFER_SIZE];
        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);
            String broadcastMsg = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
            if (!CROBOT.equals(broadcastMsg)) {
                return;
            }
            InetAddress senderAddr = receivePacket.getAddress();
            int senderPort = receivePacket.getPort();
            sendResponse(socket, senderAddr, senderPort);
        }
    }

    public void start() {
        try {
            this.socket = new DatagramSocket(port);
            this.socket.setBroadcast(true);
            this.socket.setReuseAddress(true);
            new Thread(() -> {
                try {
                    this.run(this.socket);
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


    private void sendResponse(DatagramSocket socket, InetAddress targetAddr, int targetPort) throws IOException {
        String responseMsg = "CROBOT";
        byte[] responseData = responseMsg.getBytes(StandardCharsets.UTF_8);
        DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, targetAddr, targetPort);
        socket.send(responsePacket);
    }

    public synchronized void close() {
        if (this.socket == null) {
            return;
        }
        this.socket.close();
        this.socket = null;
    }
}
