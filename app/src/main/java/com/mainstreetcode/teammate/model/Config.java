package com.mainstreetcode.teammate.model;


import android.annotation.SuppressLint;
import android.arch.core.util.Function;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammate.model.enums.BlockReason;
import com.mainstreetcode.teammate.model.enums.MetaData;
import com.mainstreetcode.teammate.model.enums.Position;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.mainstreetcode.teammate.model.enums.Visibility;
import com.mainstreetcode.teammate.repository.ConfigRepository;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;

@SuppressLint("ParcelCreator")
public class Config implements Model<Config> {

    private static final String EMPTY_STRING = "";

    public static Config empty() {return new Config(EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);}

    private String defaultTeamLogo;
    private String defaultEventLogo;
    private String defaultUserAvatar;
    private List<Sport> sports = new ArrayList<>();
    private List<Position> positions = new ArrayList<>();
    private List<Visibility> visibilities = new ArrayList<>();
    private List<BlockReason> blockReasons = new ArrayList<>();

    Config(String defaultTeamLogo, String defaultEventLogo, String defaultUserAvatar) {
        this.defaultTeamLogo = defaultTeamLogo;
        this.defaultEventLogo = defaultEventLogo;
        this.defaultUserAvatar = defaultUserAvatar;
    }

    static String getDefaultTeamLogo() {
        if (getCurrentConfig().isEmpty()) fetchConfig();
        return getCurrentConfig().defaultTeamLogo;
    }

    static String getDefaultEventLogo() {
        if (getCurrentConfig().isEmpty()) fetchConfig();
        return getCurrentConfig().defaultEventLogo;
    }

    static String getDefaultUserAvatar() {
        if (getCurrentConfig().isEmpty()) fetchConfig();
        return getCurrentConfig().defaultUserAvatar;
    }

    public static List<Sport> getSports() {
        return getList(config -> config.sports);
    }

    public static List<Position> getPositions() {
        return getList(config -> config.positions);
    }

    public static List<Visibility> getVisibilities() {
        return getList(config -> config.visibilities);
    }

    public static List<BlockReason> getBlockReasons() {
        return getList(config -> config.blockReasons);
    }

    public static Sport sportFromCode(String code) {
        return getFromCode(code, config -> config.sports, Sport.empty());
    }

    public static Position positionFromCode(String code) {
        return getFromCode(code, config -> config.positions, Position.empty());
    }

    public static Visibility visibilityFromCode(String code) {
        return getFromCode(code, config -> config.visibilities, Visibility.empty());
    }

    private static Config getCurrentConfig() {
        return ConfigRepository.getInstance().getCurrent();
    }

    private static <T> List<T> getList(Function<Config, List<T>> function) {
        Config config = getCurrentConfig();
        return config == null ? new ArrayList<>() : function.apply(config);
    }

    private static <T extends MetaData> T getFromCode(String code, Function<Config, List<T>> function, T defaultItem) {
        Config config = getCurrentConfig();
        String matcher = code != null ? code : "";
        return Flowable.fromIterable(function.apply(config))
                .filter(metaData -> matcher.equals(metaData.getCode()))
                .first(defaultItem)
                .blockingGet();
    }

    @Override
    public void update(Config updated) {
        this.defaultTeamLogo = updated.defaultTeamLogo;
        this.defaultEventLogo = updated.defaultEventLogo;
        this.defaultUserAvatar = updated.defaultUserAvatar;
        sports.addAll(updated.sports);
        positions.addAll(updated.positions);
        visibilities.addAll(updated.visibilities);
    }

    @Override
    public int compareTo(@NonNull Config o) {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return TextUtils.isEmpty(defaultTeamLogo) || sports.isEmpty() || positions.isEmpty()
                || visibilities.isEmpty() || blockReasons.isEmpty();
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

    @SuppressLint("CheckResult")
    private static void fetchConfig() {
        ConfigRepository.getInstance().get(EMPTY_STRING).subscribe(getCurrentConfig()::update, ErrorHandler.EMPTY);
    }

    public static class GsonAdapter
            implements
            JsonSerializer<Config>,
            JsonDeserializer<Config> {

        private static final String TEAM_LOGO_KEY = "defaultTeamLogo";
        private static final String EVENT_LOGO_KEY = "defaultEventLogo";
        private static final String USER_AVATAR_KEY = "defaultUserAvatar";
        private static final String SPORTS_KEY = "sports";
        private static final String POSITIONS_KEY = "roles";
        private static final String VISIBILITIES_KEY = "visibility";
        private static final String BLOCKED_REASONS_KEY = "blockReasons";

        @Override
        public JsonElement serialize(Config src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject serialized = new JsonObject();

            serialized.addProperty(TEAM_LOGO_KEY, src.defaultTeamLogo);
            serialized.addProperty(EVENT_LOGO_KEY, src.defaultEventLogo);
            serialized.addProperty(USER_AVATAR_KEY, src.defaultUserAvatar);

            JsonArray sportsArray = new JsonArray();
            JsonArray positionArray = new JsonArray();
            JsonArray visibilityArray = new JsonArray();
            JsonArray blockedReasonArray = new JsonArray();

            for (Sport item : src.sports) sportsArray.add(context.serialize(item));
            for (Position item : src.positions) positionArray.add(context.serialize(item));
            for (Visibility item : src.visibilities) visibilityArray.add(context.serialize(item));
            for (BlockReason item : src.blockReasons) blockedReasonArray.add(context.serialize(item));

            serialized.add(SPORTS_KEY, sportsArray);
            serialized.add(POSITIONS_KEY, positionArray);
            serialized.add(VISIBILITIES_KEY, visibilityArray);
            serialized.add(BLOCKED_REASONS_KEY, blockedReasonArray);

            return serialized;
        }

        @Override
        public Config deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject deviceJson = json.getAsJsonObject();

            String defaultTeamLogo = ModelUtils.asString(TEAM_LOGO_KEY, deviceJson);
            String defaultEventLogo = ModelUtils.asString(EVENT_LOGO_KEY, deviceJson);
            String defaultUserAvatar = ModelUtils.asString(USER_AVATAR_KEY, deviceJson);

            Config config = new Config(defaultTeamLogo, defaultEventLogo, defaultUserAvatar);

            ModelUtils.deserializeList(context, deviceJson.get(SPORTS_KEY), config.sports, Sport.class);
            ModelUtils.deserializeList(context, deviceJson.get(POSITIONS_KEY), config.positions, Position.class);
            ModelUtils.deserializeList(context, deviceJson.get(VISIBILITIES_KEY), config.visibilities, Visibility.class);
            ModelUtils.deserializeList(context, deviceJson.get(BLOCKED_REASONS_KEY), config.blockReasons, BlockReason.class);

            return config;
        }
    }
}
