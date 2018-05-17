package com.mainstreetcode.teammate.util;

import android.support.annotation.Nullable;
import android.support.text.emoji.EmojiCompat;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mainstreetcode.teammate.model.Identifiable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

/**
 * Static methods for models
 */

public class ModelUtils {

    public static final String EMPTY_STRING = "";
    public static final SimpleDateFormat dateFormatter;
    public static final SimpleDateFormat prettyPrinter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm", Locale.US);

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

    public static List<Identifiable> asIdentifiables(List<? extends Identifiable> subTypeList) {
        return new ArrayList<>(subTypeList);
    }

    public static <T extends Identifiable> void preserveAscending(List<T> source, List<T> additions) {
        concatenateList(source, additions);
        Collections.sort(source, Identifiable.COMPARATOR);
    }

    public static <T extends Identifiable> void preserveDescending(List<T> source, List<T> additions) {
        concatenateList(source, additions);
        Collections.sort(source, (a, b) -> -Identifiable.COMPARATOR.compare(a, b));
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T findFirst(List<Identifiable> list, Class<T> typeClass) {
        for (Identifiable item : list) if (item.getClass().equals(typeClass)) return (T) item;
        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T findLast(List<Identifiable> list, Class<T> typeClass) {
        ListIterator<Identifiable> li = list.listIterator(list.size());
        while (li.hasPrevious()) {
            Identifiable item = li.previous();
            if (item.getClass().equals(typeClass)) return ((T) item);
        }
        return null;
    }

    private static <T extends Identifiable> void concatenateList(List<T> source, List<T> additions) {
        Set<T> set = new HashSet<>(additions);
        set.addAll(source);
        source.clear();
        source.addAll(set);
    }

    public static int parse(String number) {
        if (TextUtils.isEmpty(number)) return 0;
        try { return Integer.valueOf(number); }
        catch (Exception e) { Logger.log("ModelUtils", "Number Format Exception", e);}
        return 0;
    }

    public static boolean areNotEmpty(CharSequence... values) {
        for (CharSequence value : values) if (TextUtils.isEmpty(value)) return false;
        return true;
    }

    public static CharSequence processString(CharSequence source) {
        EmojiCompat emojiCompat = EmojiCompat.get();
        return emojiCompat.getLoadState() == EmojiCompat.LOAD_STATE_SUCCEEDED ? emojiCompat.process(source) : source;
    }

    @FunctionalInterface
    public interface Consumer<T> {
        void accept(T t);
    }
}
