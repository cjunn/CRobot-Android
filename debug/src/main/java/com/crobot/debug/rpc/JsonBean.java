package com.crobot.debug.rpc;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class JsonBean {
    private static Gson gson = new Gson();
    private JsonElement element;

    private JsonBean(JsonElement object) {
        element = object;
    }

    public JsonBean(Object object) {
        element = gson.toJsonTree(object);
    }

    public JsonBean(String src) {
        element = JsonParser.parseString(src);
    }

    public int getAsInt(String name) {
        return element.getAsJsonObject().get(name).getAsInt();
    }

    public String getAsString(String name) {
        return element.getAsJsonObject().get(name).getAsString();
    }

    public JsonBean getJsonBean(String name) {
        return new JsonBean(element.getAsJsonObject().get(name).getAsJsonObject());
    }

    public <T> T asBean(Class<T> clazz) {
        return gson.fromJson(element, clazz);
    }

    private <T> List<T> asList(JsonArray array, Class<T> clz) {
        int size = array.size();
        List<T> ret = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ret.add(gson.fromJson(array.get(i), clz));
        }
        return ret;
    }

    public <T> List<T> asList(Class<T> clz) {
        return asList(element.getAsJsonArray(), clz);
    }


    public <T> List<T> getAsList(String name, Class<T> clz) {
        JsonArray array = element.getAsJsonObject().get(name).getAsJsonArray();
        return asList(array, clz);
    }


    @NonNull
    @Override
    public String toString() {
        return gson.toJson(element);
    }
}
