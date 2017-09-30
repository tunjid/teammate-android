package com.mainstreetcode.teammates.util;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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

    @Nullable
    public static LatLng parseCoordinates(String key, JsonElement source) {
        if (!source.isJsonObject()) return null;

        JsonElement element = source.getAsJsonObject().get(key);
        if (element == null || !element.isJsonArray()) return null;

        JsonArray array = element.getAsJsonArray();
        if (array.size() != 2) return null;

        JsonElement longitude = array.get(0);
        JsonElement latitude = array.get(1);

        if (!longitude.isJsonPrimitive() || !latitude.isJsonPrimitive()) return null;

        try {return new LatLng(latitude.getAsDouble(), longitude.getAsDouble());}
        catch (Exception e) {return null;}
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

    public static <T> void preserveList(List<T> source, List<T> additions) {
        Set<T> set = new HashSet<>(source);
        set.addAll(additions);
        source.clear();
        source.addAll(set);
    }
}
