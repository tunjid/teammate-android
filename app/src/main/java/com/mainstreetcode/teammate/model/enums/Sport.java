package com.mainstreetcode.teammate.model.enums;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.tunjid.androidbootstrap.core.text.SpanBuilder;

public class Sport extends MetaData {

    private static final String THONK = "\uD83E\uDD14";

    private boolean supportsLeague;
    private boolean supportsKnockout;

    private String emoji;
    private StatTypes stats = new StatTypes();

    Sport(String code, String name, String emoji, boolean supportsLeague, boolean supportsKnockout) {
        super(code, name);
        this.emoji = emoji;
        this.supportsLeague = supportsLeague;
        this.supportsKnockout = supportsKnockout;
    }

    public static Sport empty() {
        return new Sport("", App.getInstance().getString(R.string.any_sport), THONK, false, false);
    }

    public CharSequence appendEmoji(CharSequence text) {
        return new SpanBuilder(App.getInstance(), getEmoji())
                .appendCharsequence("   ")
                .appendCharsequence(text)
                .build();
    }

    public StatType statTypeFromCode(String code) {
        return stats.fromCodeOrFirst(code);
    }

    public StatTypes getStats() {
        return stats;
    }

    public CharSequence getName() { return appendEmoji(name); }

    public CharSequence getEmoji() {
        return ModelUtils.processString(emoji);
    }

    public void update(Sport updated) {
        super.update(updated);
        this.emoji = updated.emoji;
    }

    public static class GsonAdapter extends MetaData.GsonAdapter<Sport> {

        private static final String EMOJI = "emoji";
        private static final String STAT_TYPES = "statTypes";
        private static final String SUPPORTS_LEAGUE = "supportsLeague";
        private static final String SUPPORTS_KNOCKOUT = "supportsKnockout";

        @Override
        Sport fromJson(String code, String name, JsonObject body, JsonDeserializationContext context) {
            boolean supportsLeague = ModelUtils.asBoolean(SUPPORTS_LEAGUE, body);
            boolean supportsKnockout = ModelUtils.asBoolean(SUPPORTS_KNOCKOUT, body);
            String emoji = ModelUtils.asString(EMOJI, body);

            Sport sport = new Sport(code, name, emoji, supportsLeague, supportsKnockout);
            ModelUtils.deserializeList(context, body.get(STAT_TYPES), sport.stats, StatType.class);
            return sport;
        }

        @Override
        JsonObject toJson(JsonObject serialized, Sport src, JsonSerializationContext context) {
            serialized.addProperty(EMOJI, src.emoji);
            serialized.addProperty(SUPPORTS_LEAGUE, src.supportsLeague);
            serialized.addProperty(SUPPORTS_KNOCKOUT, src.supportsKnockout);
            serialized.add(STAT_TYPES, context.serialize(src.stats));
            return serialized;
        }
    }
}


