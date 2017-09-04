package com.mainstreetcode.teammates.util;

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
import java.util.TimeZone;

/**
 * Static methods for models
 */

public class ModelUtils {

    public static final SimpleDateFormat dateFormatter;

    static {
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static boolean asBoolean(String key, JsonObject jsonObject) {
        JsonElement element = jsonObject.get(key);
        return (element != null && element.isJsonPrimitive()) && element.getAsBoolean();
    }

    public static String asString(String key, JsonObject jsonObject) {
        JsonElement element = jsonObject.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : "";
    }

    public static <T> void deserializeList(JsonDeserializationContext context, JsonElement listElement,
                                           List<T> destination, Class<T> type) {
        if (listElement != null && listElement.isJsonArray()) {
            JsonArray jsonArray = listElement.getAsJsonArray();

            for (JsonElement element : jsonArray) {
                destination.add(context.deserialize(element, type));
            }
        }
    }

    public static Date parseDate(String date) {
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
