package com.auction.server.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class JsonMapper {
    private static final JsonMapper INSTANCE = new JsonMapper();

    private final Gson gson;

    private JsonMapper() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    public static JsonMapper getInstance() {
        return INSTANCE;
    }

    public String toJson(Object object) {
        return gson.toJson(object);
    }

    public <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }
}
