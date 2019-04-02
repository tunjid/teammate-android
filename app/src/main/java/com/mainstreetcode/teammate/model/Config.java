package com.mainstreetcode.teammate.model;


import android.annotation.SuppressLint;
import android.os.Parcel;
import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammate.model.enums.AndroidVariant;
import com.mainstreetcode.teammate.model.enums.BlockReason;
import com.mainstreetcode.teammate.model.enums.MetaData;
import com.mainstreetcode.teammate.model.enums.Position;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.mainstreetcode.teammate.model.enums.StatType;
import com.mainstreetcode.teammate.model.enums.TournamentStyle;
import com.mainstreetcode.teammate.model.enums.TournamentType;
import com.mainstreetcode.teammate.model.enums.Visibility;
import com.mainstreetcode.teammate.repository.ConfigRepo;
import com.mainstreetcode.teammate.repository.RepoProvider;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import io.reactivex.Flowable;
import io.reactivex.functions.Predicate;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

@SuppressLint("ParcelCreator")
public class Config implements Model<Config> {

    private static final String EMPTY_STRING = "";
    private static Config cached = Config.empty();

    public static Config empty() {return new Config(EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);}

    private String defaultTeamLogo;
    private String defaultEventLogo;
    private String defaultUserAvatar;
    private String defaultTournamentLogo;
    private List<Sport> sports = new ArrayList<>();
    private List<String> privileged = new ArrayList<>();
    private List<Position> positions = new ArrayList<>();
    private List<StatType> statTypes = new ArrayList<>();
    private List<Visibility> visibilities = new ArrayList<>();
    private List<BlockReason> blockReasons = new ArrayList<>();
    private List<AndroidVariant> staticVariants = new ArrayList<>();
    private List<TournamentType> tournamentTypes = new ArrayList<>();
    private List<TournamentStyle> tournamentStyles = new ArrayList<>();

    Config(String defaultTeamLogo, String defaultEventLogo, String defaultUserAvatar, String defaultTournamentLogo) {
        this.defaultTeamLogo = defaultTeamLogo;
        this.defaultEventLogo = defaultEventLogo;
        this.defaultUserAvatar = defaultUserAvatar;
        this.defaultTournamentLogo = defaultTournamentLogo;
    }

    static String getDefaultTeamLogo() { return getCurrentConfig().defaultTeamLogo; }

    static String getDefaultEventLogo() { return getCurrentConfig().defaultEventLogo; }

    static String getDefaultUserAvatar() { return getCurrentConfig().defaultUserAvatar; }

    static String getDefaultTournamentLogo() { return getCurrentConfig().defaultTournamentLogo; }

    public static List<Sport> getSports() { return getList(config -> config.sports); }

    @SuppressWarnings("WeakerAccess")
    public static List<String> getPrivileged() { return getList(config -> config.privileged); }

    public static List<Position> getPositions() { return getList(config -> config.positions); }

    public static List<Visibility> getVisibilities() {
        return getList(config -> config.visibilities);
    }

    public static List<BlockReason> getBlockReasons() {
        return getList(config -> config.blockReasons);
    }

    public static List<TournamentType> getTournamentTypes(Predicate<TournamentType> predicate) {
        return Flowable.fromIterable(getList(config -> config.tournamentTypes))
                .filter(predicate)
                .collect(ArrayList<TournamentType>::new, List::add).blockingGet();
    }

    public static List<TournamentStyle> getTournamentStyles(Predicate<TournamentStyle> predicate) {
        return Flowable.fromIterable(getList(config -> config.tournamentStyles))
                .filter(predicate)
                .collect(ArrayList<TournamentStyle>::new, List::add).blockingGet();
    }

    public static boolean isStaticVariant() {
        return getList(config -> config.staticVariants).contains(AndroidVariant.empty());
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

    @SuppressWarnings("WeakerAccess")
    public static BlockReason reasonFromCode(String code) {
        return getFromCode(code, config -> config.blockReasons, BlockReason.empty());
    }

    public static StatType statTypeFromCode(String code) {
        return getFromCode(code, config -> config.statTypes, StatType.empty());
    }

    public static TournamentType tournamentTypeFromCode(String code) {
        return getFromCode(code, config -> config.tournamentTypes, TournamentType.empty());
    }

    public static TournamentStyle tournamentStyleFromCode(String code) {
        return getFromCode(code, config -> config.tournamentStyles, TournamentStyle.empty());
    }

    private static Config getCurrentConfig() {
        if (cached.isEmpty()) fetchConfig();
        return cached;
    }

    private static <T> List<T> getList(Function<Config, List<T>> function) {
        Config config = getCurrentConfig();

        if (config != null && !config.isEmpty()) return function.apply(config);
        return new ArrayList<>();
    }

    private static <T extends MetaData> T getFromCode(String code, Function<Config, List<T>> function, T defaultItem) {
        Config config = getCurrentConfig();
        String matcher = code != null ? code : "";
        List<T> list = function.apply(config);
        return Flowable.fromIterable(list)
                .filter(metaData -> matcher.equals(metaData.getCode()))
                .first(list.isEmpty() ? defaultItem : list.get(0))
                .blockingGet();
    }

    @Override
    public void update(Config updated) {
        this.defaultTeamLogo = updated.defaultTeamLogo;
        this.defaultEventLogo = updated.defaultEventLogo;
        this.defaultUserAvatar = updated.defaultUserAvatar;
        ModelUtils.replaceStringList(privileged, updated.privileged);

        ModelUtils.replaceList(sports, updated.sports);
        ModelUtils.replaceList(positions, updated.positions);
        ModelUtils.replaceList(statTypes, updated.statTypes);
        ModelUtils.replaceList(visibilities, updated.visibilities);
        ModelUtils.replaceList(blockReasons, updated.blockReasons);
        ModelUtils.replaceList(staticVariants, updated.staticVariants);
        ModelUtils.replaceList(staticVariants, updated.staticVariants);
        ModelUtils.replaceList(tournamentTypes, updated.tournamentTypes);
        ModelUtils.replaceList(tournamentStyles, updated.tournamentStyles);
    }

    @Override
    public int compareTo(@NonNull Config o) {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return TextUtils.isEmpty(defaultTeamLogo) || sports.isEmpty() || positions.isEmpty()
                || visibilities.isEmpty() || blockReasons.isEmpty() || staticVariants.isEmpty();
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
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void fetchConfig() {
        if (RepoProvider.initialized()) RepoProvider.forRepo(ConfigRepo.class)
                .get(EMPTY_STRING)
                .observeOn(mainThread()) // Necessary to prevent a concurrent modification exception
                .subscribe(cached::update, ErrorHandler.EMPTY);
    }

    public static class GsonAdapter
            implements
            JsonSerializer<Config>,
            JsonDeserializer<Config> {

        private static final String TEAM_LOGO_KEY = "defaultTeamLogo";
        private static final String EVENT_LOGO_KEY = "defaultEventLogo";
        private static final String USER_AVATAR_KEY = "defaultUserAvatar";
        private static final String TOURNAMENT_LOGO_KEY = "defaultTournamentLogo";
        private static final String SPORTS_KEY = "sports";
        private static final String POSITIONS_KEY = "roles";
        private static final String PRIVILEGED = "privilegedRoles";
        private static final String VISIBILITIES_KEY = "visibility";
        private static final String BLOCKED_REASONS_KEY = "blockReasons";
        private static final String STATIC_VARIANTS_KEY = "staticAndroidVariants";
        private static final String GAME_STATS_KEY = "stats";
        private static final String TOURNAMENT_TYPE_KEY = "tournamentTypes";
        private static final String TOURNAMENT_STYLE_KEY = "tournamentStyles";

        @Override
        public JsonElement serialize(Config src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject serialized = new JsonObject();

            serialized.addProperty(TEAM_LOGO_KEY, src.defaultTeamLogo);
            serialized.addProperty(EVENT_LOGO_KEY, src.defaultEventLogo);
            serialized.addProperty(USER_AVATAR_KEY, src.defaultUserAvatar);
            serialized.addProperty(TOURNAMENT_LOGO_KEY, src.defaultTournamentLogo);

            JsonArray statsArray = new JsonArray();
            JsonArray sportsArray = new JsonArray();
            JsonArray positionArray = new JsonArray();
            JsonArray visibilityArray = new JsonArray();
            JsonArray privilegedArray = new JsonArray();
            JsonArray blockedReasonArray = new JsonArray();
            JsonArray staticVariantsArray = new JsonArray();
            JsonArray tournamentTypesArray = new JsonArray();
            JsonArray tournamentStylesArray = new JsonArray();

            for (Sport item : src.sports) sportsArray.add(context.serialize(item));
            for (StatType item : src.statTypes) statsArray.add(context.serialize(item));
            for (Position item : src.positions) positionArray.add(context.serialize(item));
            for (String item : src.privileged) privilegedArray.add(context.serialize(item));
            for (Visibility item : src.visibilities) visibilityArray.add(context.serialize(item));
            for (BlockReason item : src.blockReasons)
                blockedReasonArray.add(context.serialize(item));
            for (AndroidVariant item : src.staticVariants)
                staticVariantsArray.add(context.serialize(item));
            for (TournamentType item : src.tournamentTypes)
                tournamentTypesArray.add(context.serialize(item));
            for (TournamentStyle item : src.tournamentStyles)
                tournamentStylesArray.add(context.serialize(item));

            serialized.add(SPORTS_KEY, sportsArray);
            serialized.add(GAME_STATS_KEY, statsArray);
            serialized.add(PRIVILEGED, privilegedArray);
            serialized.add(POSITIONS_KEY, positionArray);
            serialized.add(VISIBILITIES_KEY, visibilityArray);
            serialized.add(BLOCKED_REASONS_KEY, blockedReasonArray);
            serialized.add(STATIC_VARIANTS_KEY, staticVariantsArray);
            serialized.add(TOURNAMENT_TYPE_KEY, tournamentTypesArray);
            serialized.add(TOURNAMENT_STYLE_KEY, tournamentStylesArray);

            return serialized;
        }

        @Override
        public Config deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject deviceJson = json.getAsJsonObject();

            String defaultTeamLogo = ModelUtils.asString(TEAM_LOGO_KEY, deviceJson);
            String defaultEventLogo = ModelUtils.asString(EVENT_LOGO_KEY, deviceJson);
            String defaultUserAvatar = ModelUtils.asString(USER_AVATAR_KEY, deviceJson);
            String defaultTournamentLogo = ModelUtils.asString(TOURNAMENT_LOGO_KEY, deviceJson);

            Config config = new Config(defaultTeamLogo, defaultEventLogo, defaultUserAvatar, defaultTournamentLogo);

            ModelUtils.deserializeList(context, deviceJson.get(SPORTS_KEY), config.sports, Sport.class);
            ModelUtils.deserializeList(context, deviceJson.get(PRIVILEGED), config.privileged, String.class);
            ModelUtils.deserializeList(context, deviceJson.get(POSITIONS_KEY), config.positions, Position.class);
            ModelUtils.deserializeList(context, deviceJson.get(GAME_STATS_KEY), config.statTypes, StatType.class);
            ModelUtils.deserializeList(context, deviceJson.get(VISIBILITIES_KEY), config.visibilities, Visibility.class);
            ModelUtils.deserializeList(context, deviceJson.get(BLOCKED_REASONS_KEY), config.blockReasons, BlockReason.class);
            ModelUtils.deserializeList(context, deviceJson.get(STATIC_VARIANTS_KEY), config.staticVariants, AndroidVariant.class);
            ModelUtils.deserializeList(context, deviceJson.get(TOURNAMENT_TYPE_KEY), config.tournamentTypes, TournamentType.class);
            ModelUtils.deserializeList(context, deviceJson.get(TOURNAMENT_STYLE_KEY), config.tournamentStyles, TournamentStyle.class);

            return config;
        }
    }
}
