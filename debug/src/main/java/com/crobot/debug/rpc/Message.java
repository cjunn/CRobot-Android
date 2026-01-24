package com.crobot.debug.rpc;

public abstract class Message {
    public static final int TYPE_REQUEST = 1;
    public static final int TYPE_RESPONSE = 2;

    private int type;
    private String uuid;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


}
