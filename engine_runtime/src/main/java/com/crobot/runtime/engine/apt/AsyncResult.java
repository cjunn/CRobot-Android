package com.crobot.runtime.engine.apt;

public class AsyncResult {
    private Object data;
    private String errMsg;
    public AsyncResult(Object data, String errMsg) {
        this.data = data;
        this.errMsg = errMsg;
    }
    public Object getData() {
        return data;
    }
    public String getErrMsg() {
        return errMsg;
    }

}
