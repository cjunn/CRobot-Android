package com.crobot.debug.rpc;

import com.crobot.utils.CLog;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RpcService {
    private static final String TYPE_FIELD = "type";
    private ServerSocket server;
    private Transport transport;
    private Map<String, CompletableFuture> requestMap = new ConcurrentHashMap<>();
    private MappingHandle mappingHandle = new MappingHandle(this);
    private ExecutorService invokeThread = Executors.newSingleThreadExecutor();
    private int port;

    public RpcService(int port) {
        this.port = port;
    }

    public synchronized void start() {
        if (this.server != null) {
            return;
        }
        try {
            this.server = new ServerSocket(port);
            new Thread(() -> {
                try {
                    this.run(this.server);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    close();
                }
            }).start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void close() {
        if (this.server == null) {
            return;
        }
        try {
            this.server.close();
            this.server = null;
        } catch (IOException e) {
        }
    }

    private void handleRequest(Request request) {
        invokeThread.execute(() -> mappingHandle.invoke(request));
    }

    private void handleResponse(Response response) {
        CompletableFuture future = requestMap.remove(response.getUuid());
        if (future != null) {
            future.complete(response.getData());
        }
    }


    private void run(ServerSocket server) throws IOException {
        Thread thread = Thread.currentThread();
        while (!thread.isInterrupted()) {
            try {
                Socket socket = server.accept();
                transport = new Transport(socket);
                this.sendMessage(Request.buildEmpty());
                while (!thread.isInterrupted()) {
                    String receive = transport.receive();
                    if (receive == null) {
                        break;
                    }
                    JsonBean bean = new JsonBean(receive);
                    int type = bean.getAsInt(TYPE_FIELD);
                    if (Message.TYPE_REQUEST == type) {
                        Request request = bean.asBean(Request.class);
                        handleRequest(request);
                        continue;
                    }
                    if (Message.TYPE_RESPONSE == type) {
                        Response response = bean.asBean(Response.class);
                        handleResponse(response);
                        continue;
                    }
                }
                transport.close();
            } catch (Exception e) {
                CLog.error("Socket连接出现异常", e);
            } finally {
                transport.close();
                transport = null;
            }
        }
    }


    private synchronized void sendMessage(Message message) {
        try {
            if (transport == null) {
                return;
            }
            transport.send(new JsonBean(message).toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    protected Future request(String method, Map<String, Object> data) {
        String uuid = UUID.randomUUID().toString();
        CompletableFuture future = requestMap.put(uuid, new CompletableFuture());
        sendMessage(Request.build(uuid, method, data));
        return future;
    }

    protected void successResponse(String uuid, Object data) {
        sendMessage(Response.success(uuid, data));
    }

    protected void errorResponse(String uuid, String msg) {
        sendMessage(Response.error(uuid, msg));
    }


    public synchronized <T extends ClientApi> T getClientApi(Class<T> apiClz) {
        return ClientApiBuilder.build(apiClz, this);
    }

    public void regisMapping(Object handle) {
        mappingHandle.register(handle);
    }


}
