package com.mainstreetcode.teammate.model;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Ignore;
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
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.enums.BlockReason;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SuppressLint("ParcelCreator")
public class BlockedUser implements UserHost,
        TeamHost,
        Model<BlockedUser>,
        HeaderedModel<BlockedUser>,
        ListableModel<BlockedUser> {

    private String id;
    private final User user;
    private final Team team;
    private final BlockReason reason;
    private final Date created;

    @Ignore private final List<Item<BlockedUser>> items;

    private BlockedUser(String id, User user, Team team, BlockReason reason, Date created) {
        this.id = id;
        this.user = user;
        this.team = team;
        this.reason = reason;
        this.created = created;
        items = buildItems();
    }

    public static BlockedUser block(User user, Team team, BlockReason reason) {
        return new BlockedUser("", user, team, reason, new Date());
    }

    @SuppressWarnings("unchecked")
    private List<Item<BlockedUser>> buildItems() {
        User user = getUser();
        return Arrays.asList(
                Item.text(0, Item.INPUT, R.string.first_name, user::getFirstName, user::setFirstName, this),
                Item.text(1, Item.INPUT, R.string.last_name, user::getLastName, user::setLastName, this),
                Item.text(3, Item.ROLE, R.string.team_role, reason::getCode, Item::ignore, this)
                        .textTransformer(value -> Config.reasonFromCode(value.toString()).getName())
        );
    }

    @Override
    public String getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Team getTeam() {
        return team;
    }

    public BlockReason getReason() {
        return reason;
    }

    public Date getCreated() {
        return created;
    }

    @Override
    public Item<BlockedUser> getHeaderItem() {
        return Item.text(0, Item.IMAGE, R.string.profile_picture, Item.nullToEmpty(user.getImageUrl()), Item::ignore, this);
    }

    @Override
    public List<Item<BlockedUser>> asItems() {
        return items;
    }

    @Override
    public void update(BlockedUser updated) { }

    @Override
    public boolean isEmpty() {
        return TextUtils.isEmpty(id);
    }

    @Override
    public String getImageUrl() {
        return user.getImageUrl();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    @Override
    public int compareTo(@NonNull BlockedUser o) {
        return 0;
    }

    public static class GsonAdapter implements
            JsonSerializer<BlockedUser>,
            JsonDeserializer<BlockedUser> {

        private static final String UID_KEY = "_id";
        private static final String USER_KEY = "user";
        private static final String TEAM_KEY = "team";
        private static final String REASON_KEY = "reason";
        private static final String CREATED_KEY = "created";

        @Override
        public JsonElement serialize(BlockedUser src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject serialized = new JsonObject();

            serialized.addProperty(REASON_KEY, src.reason.getCode());
            serialized.addProperty(USER_KEY, src.user.getId());
            serialized.addProperty(TEAM_KEY, src.team.getId());

            return serialized;
        }

        @Override
        public BlockedUser deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) {
                return new BlockedUser(json.getAsString(), User.empty(), Team.empty(), BlockReason.empty(), new Date());
            }

            JsonObject jsonObject = json.getAsJsonObject();

            String id = ModelUtils.asString(UID_KEY, jsonObject);
            String reasonCode = ModelUtils.asString(REASON_KEY, jsonObject);
            BlockReason reason = Config.reasonFromCode(reasonCode);
            Team team = context.deserialize(jsonObject.get(TEAM_KEY), Team.class);
            User user = context.deserialize(jsonObject.get(USER_KEY), User.class);
            Date created = ModelUtils.parseDate(ModelUtils.asString(CREATED_KEY, jsonObject));

            return new BlockedUser(id, user, team, reason, created);
        }
    }
}
