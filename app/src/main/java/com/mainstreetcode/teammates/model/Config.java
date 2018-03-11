package com.mainstreetcode.teammates.model;


import android.annotation.SuppressLint;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.lang.reflect.Type;

@SuppressLint("ParcelCreator")
public class Config implements Model<Config> {

    private String defaultTeamLogo;
    private String defaultEventLogo;
    private String defaultUserAvatar;

    Config(String defaultTeamLogo, String defaultEventLogo, String defaultUserAvatar) {
        this.defaultTeamLogo = defaultTeamLogo;
        this.defaultEventLogo = defaultEventLogo;
        this.defaultUserAvatar = defaultUserAvatar;
    }

    @Override
    public void reset() {
        defaultTeamLogo = "";
        defaultEventLogo = "";
        defaultUserAvatar = "";
    }

    @Override
    public void update(Config updated) {
        this.defaultTeamLogo = updated.defaultTeamLogo;
        this.defaultEventLogo = updated.defaultEventLogo;
        this.defaultUserAvatar = updated.defaultUserAvatar;
    }

    @Override
    public int compareTo(@NonNull Config o) {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return TextUtils.isEmpty(defaultTeamLogo);
    }

    @Override
    public String getId() {
        return "0";
    }

    @Override
    public String getImageUrl() {
        return "";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public static class GsonAdapter
            implements
            JsonSerializer<Config>,
            JsonDeserializer<Config> {

        private static final String TEAM_LOGO_KEY = "defaultTeamLogo";
        private static final String EVENT_LOGO_KEY = "defaultEventLogo";
        private static final String USER_AVATAR_KEY = "defaultUserAvatar";

        @Override
        public JsonElement serialize(Config src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject serialized = new JsonObject();

            serialized.addProperty(TEAM_LOGO_KEY, src.defaultTeamLogo);
            serialized.addProperty(EVENT_LOGO_KEY, src.defaultEventLogo);
            serialized.addProperty(USER_AVATAR_KEY, src.defaultUserAvatar);

            return serialized;
        }

        @Override
        public Config deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject deviceJson = json.getAsJsonObject();

            String defaultTeamLogo = ModelUtils.asString(TEAM_LOGO_KEY, deviceJson);
            String defaultEventLogo = ModelUtils.asString(EVENT_LOGO_KEY, deviceJson);
            String defaultUserAvatar = ModelUtils.asString(EVENT_LOGO_KEY, deviceJson);

            return new Config(defaultTeamLogo, defaultEventLogo, defaultUserAvatar);
        }
    }
}
