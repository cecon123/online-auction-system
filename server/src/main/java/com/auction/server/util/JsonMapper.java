package com.auction.server.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Singleton JSON mapper for server-side serialization and deserialization.
 *
 * This class centralizes JSON handling so that all socket messages are encoded
 * consistently.
 */
public final class JsonMapper {

    private static final JsonMapper INSTANCE = new JsonMapper();

    private final Gson gson;

    private JsonMapper() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
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
