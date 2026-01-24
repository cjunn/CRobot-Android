package com.crobot.core.util;

import java.util.Map;

public class Map2 {
    private Map map;

    public Map2(Map map) {
        this.map = map;
    }

    public Number getNumber(String key) {
        Object o = map.get(key);
        if (o instanceof Number) {
            return (Number) o;
        }
        return null;
    }

    public Boolean getBool(String key) {
        Object o = map.get(key);
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        return null;
    }


    public String getString(String key) {
        Object o = map.get(key);
        if (o instanceof String) {
            return (String) o;
        }
        return null;
    }


}
