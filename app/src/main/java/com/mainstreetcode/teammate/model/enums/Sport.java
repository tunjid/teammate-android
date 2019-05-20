/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

import static com.mainstreetcode.teammate.util.ModelUtils.replaceList;
import static com.mainstreetcode.teammate.util.ModelUtils.replaceStringList;

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
        return SpanBuilder.of(getEmoji()).append("   ").append(text).build();
    }

    public void update(Sport updated) {
        Sport source = updated;
        if (source.stats.isEmpty() || source.tournamentTypes.isEmpty() || source.tournamentStyles.isEmpty())
            source = Config.sportFromCode(source.code);

        if (this == source) return;

        super.update(source);
        this.emoji = source.emoji;
        replaceList(this.stats, source.stats);
        replaceStringList(this.tournamentTypes, source.tournamentTypes);
        replaceStringList(this.tournamentStyles, source.tournamentStyles);
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


