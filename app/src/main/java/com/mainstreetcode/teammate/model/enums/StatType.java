package com.mainstreetcode.teammate.model.enums;

import com.google.gson.JsonObject;
import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.tunjid.androidbootstrap.core.text.SpanBuilder;

import java.util.Objects;

public class StatType extends MetaData {

    private String emoji;
    private String sportCode;

    StatType(String code, String name, String emoji, String sportCode) {
        super(code, name);
        this.emoji = emoji;
        this.sportCode = sportCode;
    }

    public static StatType empty() {
        return new StatType("", "","", "");
    }

    public String getSportCode() { return sportCode; }

    public CharSequence getEmoji() { return ModelUtils.processString(emoji); }

    public CharSequence getEmojiAndName() {
        return new SpanBuilder(App.getInstance(), getEmoji())
                .appendCharsequence("   ")
                .appendCharsequence(name)
                .build();
    }

    @Override
    public String toString() {
        return name;
    }

    public void update(StatType updated) {
        super.update(updated);
        this.emoji = updated.emoji;
        this.sportCode = updated.sportCode;
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
        private static final String SPORT = "sport";

        @Override
        StatType fromJson(String code, String name, JsonObject body) {
            String emoji = ModelUtils.asString(EMOJI_KEY, body);
            String sportCode = ModelUtils.asString(SPORT, body);
            return new StatType(code, name, emoji, sportCode);
        }

        @Override
        JsonObject toJson(JsonObject serialized, StatType src) {
            serialized.addProperty(EMOJI_KEY, src.emoji);
            return serialized;
        }
    }
}
