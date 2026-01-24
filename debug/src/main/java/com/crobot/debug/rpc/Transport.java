package com.crobot.debug.rpc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Transport {
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;


    public Transport(Socket socket) throws IOException {
        this.socket = socket;
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    }

    public synchronized void send(String message) throws IOException {
        this.writer.write(message);
        this.writer.newLine();
        this.writer.flush();
    }

    public String receive() throws IOException {
        return this.reader.readLine();
    }

    public synchronized void close() throws IOException {
        if (socket != null) {
            this.writer.close();
            this.reader.close();
            this.socket.close();
            this.socket = null;
        }
    }

    public Socket getSocket() {
        return socket;
    }
}
