package com.mainstreetcode.teammate.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.lang.reflect.Type;

public class Sport {

    private String code;
    private String name;

    Sport(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static Sport empty() {
        return new Sport("", "");
    }

    public String getCode() {
        return code;
    }

    public static class GsonAdapter implements JsonDeserializer<Sport> {

        private static final String CODE_KEY = "code";
        private static final String NAME_KEY = "name";

        @Override
        public Sport deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject sportJson = json.getAsJsonObject();

            String code = ModelUtils.asString(CODE_KEY, sportJson);
            String name = ModelUtils.asString(NAME_KEY, sportJson);

            return new Sport(code, name);
        }
    }
}


