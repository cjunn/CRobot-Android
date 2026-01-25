package com.crobot.runtime.engine;

public class Result {
    private Object value;
    private String message;

    public Result(Object value) {
        this.value = value == null ? "" : value;
    }

    public static Result error(String message) {
        Result result = new Result(null);
        result.message = message;
        return result;
    }


    public double asDouble() {
        if (message != null) {
            throw new ContextException(message);
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0;
    }

    public float asFloat() {
        if (message != null) {
            throw new ContextException(message);
        }
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return 0;
    }

    public int asInt() {
        if (message != null) {
            throw new ContextException(message);
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    public String asString() {
        if (message != null) {
            throw new ContextException(message);
        }
        if (isEmpty(this.value)) {
            return null;
        }
        return this.value.toString();
    }

    public boolean asBoolean() {
        if (message != null) {
            throw new ContextException(message);
        }
        if (isEmpty(this.value)) {
            return Boolean.FALSE;
        }
        try {
            return Boolean.valueOf(this.value.toString());
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    }

    public Object getObject() {
        if (message != null) {
            throw new ContextException(message);
        }
        return this.value;
    }

    private boolean isEmpty(Object value) {
        return value == null;
    }
}
