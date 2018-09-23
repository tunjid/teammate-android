package com.mainstreetcode.teammate.model.enums;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.util.Objects;

public class TournamentType extends MetaData {

    private String refPath;

    TournamentType(String code, String name, String refPath) {
        super(code, name);
        this.refPath = refPath;
    }

    public static TournamentType empty() {
        return new TournamentType("", "", "");
    }

    public String getRefPath() { return refPath; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TournamentType)) return false;
        TournamentType variant = (TournamentType) o;
        return Objects.equals(code, variant.code) && Objects.equals(name, variant.name);    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name);
    }

    public static class GsonAdapter extends MetaData.GsonAdapter<TournamentType> {

        private static final String REF_PATH = "refPath";

        @Override
        TournamentType fromJson(String code, String name, JsonObject body, JsonDeserializationContext context) {
            String refPath = ModelUtils.asString(REF_PATH, body);
            return new TournamentType(code, name, refPath);
        }

        @Override
        JsonObject toJson(JsonObject serialized, TournamentType src, JsonSerializationContext context) {
            serialized.addProperty(REF_PATH, src.refPath);
            return serialized;
        }
    }
}
