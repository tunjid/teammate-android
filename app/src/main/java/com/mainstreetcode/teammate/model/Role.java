package com.mainstreetcode.teammate.model;

import android.arch.persistence.room.Ignore;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.enums.Position;
import com.mainstreetcode.teammate.persistence.entity.RoleEntity;
import com.mainstreetcode.teammate.util.IdCache;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.tunjid.androidbootstrap.core.text.SpanBuilder;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.mainstreetcode.teammate.util.ModelUtils.EMPTY_STRING;

/**
 * Roles on a team
 */

public class Role extends RoleEntity
        implements
        UserHost,
        TeamHost,
        Model<Role>,
        HeaderedModel<Role>,
        ListableModel<Role> {

    public static final String PHOTO_UPLOAD_KEY = "role-photo";

    @Ignore private static final IdCache holder = IdCache.cache(5);

    public Role(String id, String imageUrl, String nickname, Position position, Team team, User user, Date created) {
        super(id, imageUrl, nickname, position, team, user, created);
    }

    protected Role(Parcel in) {
        super(in);
    }

    public static Role empty() {
        return new Role("", Config.getDefaultUserAvatar(), "", Position.empty(), Team.empty(), User.empty(), new Date());
    }

    @Override
    public List<Item<Role>> asItems() {
        User user = getUser();
        return Arrays.asList(
                Item.text(holder.get(0), 0, Item.INPUT, R.string.first_name, user::getFirstName, user::setFirstName, this),
                Item.text(holder.get(1), 1, Item.INPUT, R.string.last_name, user::getLastName, user::setLastName, this),
                Item.text(holder.get(2), 2, Item.NICKNAME, R.string.nickname, this::getNickname, this::setNickname, this),
                Item.text(holder.get(3), 3, Item.ABOUT, R.string.user_about, user::getAbout, Item::ignore, this),
                Item.text(holder.get(4), 4, Item.ROLE, R.string.team_role, position::getCode, this::setPosition, this)
                        .textTransformer(value -> Config.positionFromCode(value.toString()).getName())
        );
    }

    @Override
    public Item<Role> getHeaderItem() {
        return Item.text(EMPTY_STRING, 0, Item.IMAGE, R.string.profile_picture, Item.nullToEmpty(imageUrl), this::setImageUrl, this);
    }

    @Override
    public boolean areContentsTheSame(Identifiable other) {
        if (!(other instanceof Role)) return id.equals(other.getId());
        Role casted = (Role) other;
        return position.equals(casted.position)
                && user.areContentsTheSame(casted.getUser())
                && team.areContentsTheSame(casted.team);
    }

    @Override
    public Object getChangePayload(Identifiable other) {
        return other;
    }

    @Override
    public boolean isEmpty() {
        return TextUtils.isEmpty(id);
    }

    @Override
    public void update(Role updated) {
        this.id = updated.getId();
        this.imageUrl = updated.imageUrl;
        this.nickname = updated.nickname;
        this.position.update(updated.position);
        if (updated.team.hasMajorFields()) this.team.update(updated.team);
        if (updated.user.hasMajorFields()) this.user.update(updated.user);
    }

    @Override
    public int compareTo(@NonNull Role o) {
        int roleComparison = position.getCode().compareTo(o.position.getCode());
        int userComparison = user.compareTo(o.user);
        int teamComparison = team.compareTo(o.team);

        return roleComparison != 0
                ? roleComparison
                : userComparison != 0
                ? userComparison
                : teamComparison != 0
                ? teamComparison
                : id.compareTo(o.id);
    }

    public boolean isPrivilegedRole() {
        String positionCode = position != null ? position.getCode() : "";
        return !TextUtils.isEmpty(positionCode) && !isEmpty() && Config.getPrivileged().contains(positionCode);
    }

    public CharSequence getTitle() {
        CharSequence title = user.getFirstName();
        if (!TextUtils.isEmpty(nickname)) return new SpanBuilder(App.getInstance(), title)
                .appendNewLine().appendCharsequence("\"" + nickname + "\"")
                .build();

        return title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public static final Parcelable.Creator<Role> CREATOR = new Parcelable.Creator<Role>() {
        @Override
        public Role createFromParcel(Parcel in) {
            return new Role(in);
        }

        @Override
        public Role[] newArray(int size) {
            return new Role[size];
        }
    };

    public static class GsonAdapter
            implements
            JsonSerializer<Role>,
            JsonDeserializer<Role> {

        private static final String ID_KEY = "_id";
        private static final String NAME_KEY = "name";
        private static final String USER_KEY = "user";
        private static final String TEAM_KEY = "team";
        private static final String IMAGE_KEY = "imageUrl";
        private static final String CREATED_KEY = "created";
        private static final String NICK_NAME_KEY = "nickname";

        @Override
        public JsonElement serialize(Role src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject serialized = new JsonObject();

            serialized.addProperty(ID_KEY, src.id);
            serialized.addProperty(IMAGE_KEY, src.imageUrl);
            serialized.addProperty(NICK_NAME_KEY, src.nickname);
            serialized.add(USER_KEY, context.serialize(src.user));

            String positionCode = src.position != null ? src.position.getCode() : "";
            if (!TextUtils.isEmpty(positionCode)) serialized.addProperty(NAME_KEY, positionCode);

            return serialized;
        }

        @Override
        public Role deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonObject roleJson = json.getAsJsonObject();

            String id = ModelUtils.asString(ID_KEY, roleJson);
            String imageUrl = ModelUtils.asString(IMAGE_KEY, roleJson);
            String nickname = ModelUtils.asString(NICK_NAME_KEY, roleJson);
            String positionName = ModelUtils.asString(NAME_KEY, roleJson);

            Position position = Config.positionFromCode(positionName);
            Team team = context.deserialize(roleJson.get(TEAM_KEY), Team.class);
            User user = context.deserialize(roleJson.get(USER_KEY), User.class);
            Date created = ModelUtils.parseDate(ModelUtils.asString(CREATED_KEY, roleJson));

            if (user == null) user = User.empty();

            return new Role(id, imageUrl, nickname, position, team, user, created);
        }
    }
}
