package com.mainstreetcode.teammate.model.enums;

import com.google.gson.JsonObject;
import com.mainstreetcode.teammate.util.ModelUtils;

public class Sport extends MetaData {

    private static final String THONK = "\uD83E\uDD14";

    private String emoji;

    Sport(String code, String name, String emoji) {
        super(code, name);
        this.emoji = emoji;
    }

    public static Sport empty() {
        return new Sport("", "", THONK);
    }

    public String appendEmoji(String text) { return emoji + "   " + text; }

    public String getName() { return appendEmoji(name); }

    public void reset() {
        super.reset();
        this.emoji = THONK;
    }

    public void update(Sport updated) {
        super.update(updated);
        this.emoji = updated.emoji;
    }

    public static class GsonAdapter extends MetaData.GsonAdapter<Sport> {

        private static final String EMOJI_KEY = "emoji";

        @Override
        Sport with(String code, String name, JsonObject body) {
            String emoji = ModelUtils.asString(EMOJI_KEY, body);
            return new Sport(code, name, emoji);
        }
    }
}


