package com.mainstreetcode.teammate.model.enums;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import java.util.Objects;

public class StatAttribute extends MetaData {

    StatAttribute(String code, String name) {
        super(code, name);
    }

    public static StatAttribute empty() {
        return new StatAttribute("", "");
    }

    @Override
    public String toString() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StatAttribute)) return false;
        StatAttribute variant = (StatAttribute) o;
        return Objects.equals(code, variant.code) && Objects.equals(name, variant.name);    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name);
    }

    public static class GsonAdapter extends MetaData.GsonAdapter<StatAttribute> {
        @Override
        StatAttribute fromJson(String code, String name, JsonObject body, JsonDeserializationContext context) {
            return new StatAttribute(code, name);
        }
    }
}
