package com.mainstreetcode.teammate.util;

import android.text.TextUtils;

import com.google.android.gms.common.util.BiConsumer;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tunjid.androidbootstrap.functions.Supplier;
import com.tunjid.androidbootstrap.functions.collections.Lists;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;

import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.emoji.text.EmojiCompat;

import static com.tunjid.androidbootstrap.functions.collections.Lists.transform;

/**
 * Static methods for models
 */

public class ModelUtils {

    public static final String EMPTY_STRING = "";
    public static final SimpleDateFormat dateFormatter;
    public static final SimpleDateFormat prettyPrinter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm", Locale.US);

    private static final Pattern alphaNumeric = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);

    static {
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    static boolean hasNoSpecialChars(CharSequence sequence) {
        return !alphaNumeric.matcher(sequence).find();
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

    public static <K, V> V get(K key, Map<K, V> map, Supplier<V> instantiator) {
        return get(key, map::get, map::put, instantiator);
    }

    public static <K, V> V get(K key, Function<K, V> getter, BiConsumer<K, V> setter, Supplier<V> instantiator) {
        V value = getter.apply(key);
        if (value == null) setter.accept(key, value = instantiator.get());

        return value;
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
        try { return formatter.parse(date); }
        catch (ParseException e) { return new Date(); }
    }

    public static List<Differentiable> asDifferentiables(List<? extends Differentiable> subTypeList) {
        return new ArrayList<>(subTypeList);
    }

    public static void replaceStringList(List<String> sourceList, List<String> updatedList) {
        List<Differentiable> source = transform(sourceList, s -> Differentiable.fromCharSequence(() -> s), Differentiable::getId);
        List<Differentiable> updated = transform(updatedList, s -> Differentiable.fromCharSequence(() -> s), Differentiable::getId);
        replaceList(source, updated);
    }

    public static <T extends Differentiable> void preserveAscending(List<T> source, List<T> additions) {
        concatenateList(source, additions);
        Collections.sort(source, FunctionalDiff.COMPARATOR);
    }

    public static <T extends Differentiable> void preserveDescending(List<T> source, List<T> additions) {
        concatenateList(source, additions);
        Collections.sort(source, FunctionalDiff.DESCENDING_COMPARATOR);
    }

    public static <T extends Differentiable> List<T> replaceList(List<T> source, List<T> additions) {
        Lists.replace(source, additions);
        Collections.sort(source, FunctionalDiff.COMPARATOR);
        return source;
    }

    private static <T extends Differentiable> void concatenateList(List<T> source, List<T> additions) {
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

    public static float parseFloat(String number) {
        if (TextUtils.isEmpty(number)) return 0;
        try { return Float.valueOf(number); }
        catch (Exception e) { Logger.log("ModelUtils", "Number Format Exception", e);}
        return 0;
    }

    public static boolean parseBoolean(String number) {
        if (TextUtils.isEmpty(number)) return false;
        try { return Boolean.valueOf(number); }
        catch (Exception e) { Logger.log("ModelUtils", "Number Format Exception", e);}
        return false;
    }

    public static boolean areNotEmpty(CharSequence... values) {
        for (CharSequence value : values) if (TextUtils.isEmpty(value)) return false;
        return true;
    }

    public static CharSequence processString(CharSequence source) {
        EmojiCompat emojiCompat = EmojiCompat.get();
        return emojiCompat.getLoadState() == EmojiCompat.LOAD_STATE_SUCCEEDED ? emojiCompat.process(source) : source;
    }

}
