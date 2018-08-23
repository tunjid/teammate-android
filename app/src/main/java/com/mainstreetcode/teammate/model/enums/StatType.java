package com.mainstreetcode.teammate.model.enums;

import com.google.gson.JsonObject;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.util.Objects;

public class StatType extends MetaData {

    private String emoji;

    StatType(String code, String name, String emoji) {
        super(code, name);
        this.emoji = emoji;
    }

    public static StatType empty() {
        return new StatType("", "","");
    }

    public CharSequence getEmoji() {
        return ModelUtils.processString(emoji);
    }

    public void update(StatType updated) {
        super.update(updated);
        this.emoji = updated.emoji;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StatType)) return false;
        StatType variant = (StatType) o;
        return Objects.equals(code, variant.code) && Objects.equals(name, variant.name);    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name);
    }

    public static class GsonAdapter extends MetaData.GsonAdapter<StatType> {

        private static final String EMOJI_KEY = "emoji";

        @Override
        StatType fromJson(String code, String name, JsonObject body) {
            String emoji = ModelUtils.asString(EMOJI_KEY, body);
            return new StatType(code, name, emoji);
        }

        @Override
        JsonObject toJson(JsonObject serialized, StatType src) {
            serialized.addProperty(EMOJI_KEY, src.emoji);
            return serialized;
        }
    }
}
