package com.auction.client.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.time.LocalDateTime;

/**
 * Singleton JSON mapper for client-side serialization and deserialization.
 */
public final class JsonMapper {

    private static final JsonMapper INSTANCE = new JsonMapper();

    private final Gson gson;

    private JsonMapper() {
        this.gson = new GsonBuilder()
            .registerTypeAdapter(
                LocalDateTime.class,
                new LocalDateTimeAdapter()
            )
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

    public <T> T fromJson(String json, Type type) {
        return gson.fromJson(json, type);
    }

    /**
     * Converts generic data into the expected DTO class.
     */
    public <T> T convertData(Object data, Class<T> clazz) {
        if (data == null) {
            return null;
        }
        if (clazz.isInstance(data)) {
            return clazz.cast(data);
        }
        return fromJson(toJson(data), clazz);
    }

    /**
     * Converts generic data into a list of the expected DTO class.
     */
    public <T> java.util.List<T> convertList(Object data, Class<T> clazz) {
        if (data == null) {
            return java.util.Collections.emptyList();
        }
        String json = toJson(data);
        Type listType = com.google.gson.reflect.TypeToken.getParameterized(java.util.List.class, clazz).getType();
        return gson.fromJson(json, listType);
    }

    private static final class LocalDateTimeAdapter
        implements
            JsonSerializer<LocalDateTime>,
            JsonDeserializer<LocalDateTime>
    {

        @Override
        public JsonElement serialize(
            LocalDateTime src,
            Type typeOfSrc,
            JsonSerializationContext context
        ) {
            return src == null ? null : new JsonPrimitive(src.toString());
        }

        @Override
        public LocalDateTime deserialize(
            JsonElement json,
            Type typeOfT,
            JsonDeserializationContext context
        ) {
            if (json == null || json.isJsonNull()) {
                return null;
            }
            return LocalDateTime.parse(json.getAsString());
        }
    }
}
