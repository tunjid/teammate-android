package com.mainstreetcode.teammate.model.enums;

import com.google.gson.JsonObject;
import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.tunjid.androidbootstrap.core.text.SpanBuilder;

public class Sport extends MetaData {

    private static final String THONK = "\uD83E\uDD14";

    private String emoji;

    Sport(String code, String name, String emoji) {
        super(code, name);
        this.emoji = emoji;
    }

    public static Sport empty() {
        return new Sport("", App.getInstance().getString(R.string.any_sport), THONK);
    }

    public CharSequence appendEmoji(CharSequence text) {
        return new SpanBuilder(App.getInstance(), getEmoji())
                .appendCharsequence("   ")
                .appendCharsequence(text)
                .build();
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

        private static final String EMOJI_KEY = "emoji";

        @Override
        Sport fromJson(String code, String name, JsonObject body) {
            String emoji = ModelUtils.asString(EMOJI_KEY, body);
            return new Sport(code, name, emoji);
        }

        @Override
        JsonObject toJson(JsonObject serialized, Sport src) {
            serialized.addProperty(EMOJI_KEY, src.emoji);
            return serialized;
        }
    }
}


