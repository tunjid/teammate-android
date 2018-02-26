package com.mainstreetcode.teammates.util;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.Message;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import retrofit2.HttpException;

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
        try { return (element != null && element.isJsonPrimitive()) && element.getAsBoolean();}
        catch (Exception e) {return false;}
    }

    public static String asString(String key, JsonObject jsonObject) {
        JsonElement element = jsonObject.get(key);
        try { return element != null && element.isJsonPrimitive() ? element.getAsString() : "";}
        catch (Exception e) {return "";}
    }

    public static float asFloat(String key, JsonObject jsonObject) {
        JsonElement element = jsonObject.get(key);
        try {return element != null && element.isJsonPrimitive() ? element.getAsFloat() : 0;}
        catch (Exception e) {return 0;}
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
        Date result;
        synchronized (dateFormatter) {result = parseDate(date, dateFormatter);}
        return result;
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

    @Nullable
    public static Message fromThrowable(Throwable throwable) {
        if (!(throwable instanceof HttpException)) return null;
        return new Message((HttpException) throwable);
    }

    public static <T extends Identifiable> void preserveList(List<T> source, List<T> additions) {
        concatenateList(source, additions);
        Collections.sort(source, Identifiable.COMPARATOR);
    }

    public static <T extends Identifiable> void preserveListInverse(List<T> source, List<T> additions) {
        concatenateList(source, additions);
        Collections.sort(source, (a, b) -> -Identifiable.COMPARATOR.compare(a, b));
    }

    private static <T extends Identifiable> void concatenateList(List<T> source, List<T> additions) {
        Set<T> set = new HashSet<>(additions);
        set.addAll(source);
        source.clear();
        source.addAll(set);
    }
}
