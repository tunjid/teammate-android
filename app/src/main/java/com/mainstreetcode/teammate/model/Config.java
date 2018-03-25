package com.mainstreetcode.teammate.model;


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
import com.mainstreetcode.teammate.repository.ConfigRepository;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.lang.reflect.Type;

@SuppressLint("ParcelCreator")
public class Config implements Model<Config> {

    private static final String EMPTY_STRING = "";
    private static Config currentConfig = new Config(EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);

    private String defaultTeamLogo;
    private String defaultEventLogo;
    private String defaultUserAvatar;

    Config(String defaultTeamLogo, String defaultEventLogo, String defaultUserAvatar) {
        this.defaultTeamLogo = defaultTeamLogo;
        this.defaultEventLogo = defaultEventLogo;
        this.defaultUserAvatar = defaultUserAvatar;
    }

    static String getDefaultTeamLogo() {
        if (currentConfig.isEmpty()) fetchConfig();
        return currentConfig.defaultTeamLogo;
    }

    static String getDefaultEventLogo() {
        if (currentConfig.isEmpty()) fetchConfig();
        return currentConfig.defaultEventLogo;
    }

    static String getDefaultUserAvatar() {
        if (currentConfig.isEmpty()) fetchConfig();
        return currentConfig.defaultUserAvatar;
    }

    @Override
    public void reset() {
        defaultTeamLogo = EMPTY_STRING;
        defaultEventLogo = EMPTY_STRING;
        defaultUserAvatar = EMPTY_STRING;
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

    private static void fetchConfig() {
        ConfigRepository.getInstance().get(EMPTY_STRING).subscribe(currentConfig::update, ErrorHandler.EMPTY);
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
            String defaultUserAvatar = ModelUtils.asString(USER_AVATAR_KEY, deviceJson);

            return new Config(defaultTeamLogo, defaultEventLogo, defaultUserAvatar);
        }
    }
}
