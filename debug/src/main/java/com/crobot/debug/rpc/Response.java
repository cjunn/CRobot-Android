package com.crobot.debug.rpc;

public class Response extends Message {
    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_ERROR = -1;

    private int status;
    private String msg;
    private Object data;

    public Response(String uuid) {
        this.setType(Message.TYPE_RESPONSE);
        this.setUuid(uuid);
    }

    private static Message buildResponse(String uuid, Integer status, String msg, Object data) {
        Response message = new Response(uuid);
        message.setData(data);
        message.setStatus(status);
        message.setMsg(msg);
        return message;
    }

    public static Message success(String uuid, Object data) {
        return buildResponse(uuid, Response.STATUS_SUCCESS, "", data);
    }

    public static Message error(String uuid, String msg) {
        return buildResponse(uuid, Response.STATUS_ERROR, msg, null);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
