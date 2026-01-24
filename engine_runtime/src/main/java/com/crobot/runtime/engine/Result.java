package com.crobot.runtime.engine;

public class Result {
    private Object value;

    public Result(Object value) {
        this.value = value == null ? "" : value;
    }

    public double asDouble() {
        if(value instanceof Number){
            return ((Number)value).doubleValue();
        }
        return 0;
    }

    public float asFloat() {
        if(value instanceof Number){
            return ((Number)value).floatValue();
        }
        return 0;
    }

    public int asInt() {
        if(value instanceof Number){
            return ((Number)value).intValue();
        }
        return 0;
    }

    public String asString() {
        if (isEmpty(this.value)) {
            return null;
        }
        return this.value.toString();
    }

    public boolean asBoolean() {
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
        return this.value;
    }


    private boolean isEmpty(Object value) {
        return value == null;
    }
}
