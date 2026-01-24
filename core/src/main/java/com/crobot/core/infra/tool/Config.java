package com.crobot.core.infra.tool;

import java.util.Map;

public interface Config {
    Object get(String key);

    Map<String, Object> getAll();

    void set(String key, Object value);
}
