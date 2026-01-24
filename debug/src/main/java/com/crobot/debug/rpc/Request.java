package com.crobot.debug.rpc;

import java.util.Map;

public class Request extends Message {
    private String method;
    private Map<String, Object> data;

    public Request(String uuid) {
        this.setType(Message.TYPE_REQUEST);
        this.setUuid(uuid);
    }

    public static Message buildEmpty() {
        return build("", "", null);
    }

    public static Message build(String uuid, String method, Map<String, Object> data) {
        Request message = new Request(uuid);
        message.setMethod(method);
        message.setData(data);
        return message;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

}
