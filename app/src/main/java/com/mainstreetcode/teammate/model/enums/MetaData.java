package com.mainstreetcode.teammate.model.enums;

import android.text.TextUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.lang.reflect.Type;
import java.util.Objects;

public class MetaData {

    private String code;
    protected String name;

    MetaData(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public boolean isInvalid() {
        return TextUtils.isEmpty(code);
    }

    public String getName() { return name; }

    public String getCode() { return code; }

    public void reset() {
        this.code = "";
        this.name = "";
    }

    public void update(MetaData updated) {
        this.code = updated.code;
        this.name = updated.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetaData)) return false;
        MetaData metaData = (MetaData) o;
        return Objects.equals(code, metaData.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    public static abstract class GsonAdapter<T extends MetaData> implements JsonDeserializer<T> {

        private static final String CODE_KEY = "code";
        private static final String NAME_KEY = "name";

        @Override
        public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject baseEnumJson = json.getAsJsonObject();

            String code = ModelUtils.asString(CODE_KEY, baseEnumJson);
            String name = ModelUtils.asString(NAME_KEY, baseEnumJson);

            return with(code, name, baseEnumJson);
        }

        abstract T with(String code, String name, JsonObject body);
    }
}


