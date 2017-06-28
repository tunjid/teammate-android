package com.mainstreetcode.teammates.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * Static methods for models
 * <p>
 * Created by Shemanigans on 6/16/17.
 */

class ModelUtils {

    static boolean asBoolean(String key, JsonObject jsonObject) {
        JsonElement element = jsonObject.get(key);
        return (element != null && element.isJsonPrimitive()) && element.getAsBoolean();
    }

    static String asString(String key, JsonObject jsonObject) {
        JsonElement element = jsonObject.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
    }

    static <T> void deserializeList(JsonDeserializationContext context, JsonElement listElement,
                                    List<T> destination, Class<T> type) {
        if (listElement != null && listElement.isJsonArray()) {
            JsonArray jsonArray = listElement.getAsJsonArray();

            for (JsonElement element : jsonArray) {
                destination.add(context.deserialize(element, type));
            }
        }
    }
}
