package com.mainstreetcode.teammates.model;

import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Static methods for models
 */

public class ModelUtils {

    static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);

    static boolean asBoolean(String key, JsonObject jsonObject) {
        JsonElement element = jsonObject.get(key);
        return (element != null && element.isJsonPrimitive()) && element.getAsBoolean();
    }

    static String asString(String key, JsonObject jsonObject) {
        JsonElement element = jsonObject.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : "";
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

    static Date parseDate(String date) {
        return parseDate(date, dateFormatter);
    }

    public static Date parseDate(String date, SimpleDateFormat formatter) {
        if (TextUtils.isEmpty(date)) return new Date();
        try {
            return formatter.parse(date);
        }
        catch (ParseException e) {
            return new Date();
        }
    }
}
