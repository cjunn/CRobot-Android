package com.crobot.core.infra.tool;

import com.crobot.core.util.ReTimer;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ConfigImpl implements Config {
    private final JSONObject values;
    private ReTimer flushTimer = new ReTimer();
    private String module;
    private FileOperation fileOperation;

    public ConfigImpl(FileOperation fileOperation, String module) {
        this.fileOperation = fileOperation;
        this.module = module;
        this.values = initJSONObject();
    }

    public JSONObject toJsonObject() {
        String json = fileOperation.readText(module);
        if (json == null || json.trim().length() == 0) {
            return null;
        }
        try {
            return new JSONObject(json);
        } catch (Exception e) {
            return null;
        }
    }


    public JSONObject initJSONObject() {
        JSONObject json = toJsonObject();
        if (json != null) {
            return json;
        }
        return new JSONObject();
    }

    private synchronized boolean putMap(String key, Object value) {
        try {
            values.put(key, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private synchronized Object getMap(String key) {
        return values.opt(key);
    }

    private synchronized Map<String, Object> getMaps() {
        Iterator<String> keys = values.keys();
        Map<String, Object> retMap = new HashMap<>();
        for (Iterator<String> it = keys; it.hasNext(); ) {
            String key = it.next();
            retMap.put(key, values.opt(key));
        }
        return retMap;
    }

    private synchronized void flushToDisk() {
        fileOperation.writeText(this.module, values.toString());
    }

    @Override
    public void set(String key, Object value) {
        putMap(key, value);
        flushTimer.submit(() -> flushToDisk(), 1000);
    }

    @Override
    public Object get(String key) {
        return getMap(key);
    }

    @Override
    public Map<String, Object> getAll() {
        return getMaps();
    }

}
