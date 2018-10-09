package com.mainstreetcode.teammate.model.enums;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.tunjid.androidbootstrap.core.text.SpanBuilder;

import java.util.ArrayList;
import java.util.List;

public class Sport extends MetaData {

    private static final String THONK = "\uD83E\uDD14";

    private String emoji;
    private StatTypes stats = new StatTypes();
    private List<String> tournamentTypes = new ArrayList<>();
    private List<String> tournamentStyles = new ArrayList<>();

    private Sport(String code, String name, String emoji) {
        super(code, name);
        this.emoji = emoji;
    }

    public static Sport empty() {
        return new Sport("", App.getInstance().getString(R.string.any_sport), THONK);
    }

    public boolean supportsCompetitions() { return !tournamentStyles.isEmpty(); }

    public boolean supportsTournamentType(TournamentType type) {
        return tournamentTypes.contains(type.code);
    }

    public boolean supportsTournamentStyle(TournamentStyle style) {
        return tournamentStyles.contains(style.code);
    }

    public boolean betweenUsers() {
        return User.COMPETITOR_TYPE.equals(refType());
    }

    public String refType() {
        return tournamentTypes.isEmpty() ? "" : Config.tournamentTypeFromCode(tournamentTypes.get(0)).getRefPath();
    }

    public TournamentType defaultTournamentType() {
        return tournamentTypes.isEmpty()
                ? TournamentType.empty()
                : Config.tournamentTypeFromCode(tournamentTypes.get(0));
    }

    public TournamentStyle defaultTournamentStyle() {
        return tournamentStyles.isEmpty()
                ? TournamentStyle.empty()
                : Config.tournamentStyleFromCode(tournamentStyles.get(0));
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

    public CharSequence appendEmoji(CharSequence text) {
        return new SpanBuilder(App.getInstance(), getEmoji())
                .appendCharsequence("   ")
                .appendCharsequence(text)
                .build();
    }

    public void update(Sport updated) {
        super.update(updated);
        if (this == updated) return;
        this.emoji = updated.emoji;
        if (!updated.tournamentTypes.isEmpty()) {
            tournamentTypes.clear();
            tournamentTypes.addAll(updated.tournamentTypes);
        }
        if (!updated.tournamentStyles.isEmpty()) {
            tournamentStyles.clear();
            tournamentStyles.addAll(updated.tournamentStyles);
        }
    }

    public static class GsonAdapter extends MetaData.GsonAdapter<Sport> {

        private static final String EMOJI = "emoji";
        private static final String STAT_TYPES = "statTypes";
        private static final String TOURNAMENT_TYPES = "tournamentTypes";
        private static final String TOURNAMENT_STYLES = "tournamentStyles";

        @Override
        Sport fromJson(String code, String name, JsonObject body, JsonDeserializationContext context) {
            String emoji = ModelUtils.asString(EMOJI, body);

            Sport sport = new Sport(code, name, emoji);
            ModelUtils.deserializeList(context, body.get(STAT_TYPES), sport.stats, StatType.class);
            ModelUtils.deserializeList(context, body.get(TOURNAMENT_TYPES), sport.tournamentTypes, String.class);
            ModelUtils.deserializeList(context, body.get(TOURNAMENT_STYLES), sport.tournamentStyles, String.class);

            return sport;
        }

        @Override
        JsonObject toJson(JsonObject serialized, Sport src, JsonSerializationContext context) {
            serialized.addProperty(EMOJI, src.emoji);
            serialized.add(STAT_TYPES, context.serialize(src.stats));
            serialized.add(TOURNAMENT_TYPES, context.serialize(src.tournamentTypes));
            serialized.add(TOURNAMENT_STYLES, context.serialize(src.tournamentStyles));
            return serialized;
        }
    }
}


