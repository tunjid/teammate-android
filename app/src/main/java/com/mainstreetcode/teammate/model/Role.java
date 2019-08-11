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

package com.mainstreetcode.teammate.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.enums.Position;
import com.mainstreetcode.teammate.persistence.entity.RoleEntity;
import com.mainstreetcode.teammate.util.IdCache;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.tunjid.androidbootstrap.core.text.SpanBuilder;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.Ignore;

import static com.mainstreetcode.teammate.util.ModelUtils.EMPTY_STRING;

/**
 * Roles on a team
 */

public class Role extends RoleEntity
        implements
        UserHost,
        TeamHost,
        HeaderedModel<Role>,
        ListableModel<Role>,
        TeamMemberModel<Role> {

    public static final String PHOTO_UPLOAD_KEY = "role-photo";

    @Ignore private static final IdCache holder = IdCache.cache(5);

    public Role(String id, String imageUrl, String nickname, Position position, Team team, User user, Date created) {
        super(id, imageUrl, nickname, position, team, user, created);
    }

    protected Role(Parcel in) {
        super(in);
    }

    public static Role empty() {
        return new Role("", Config.getDefaultUserAvatar(), "", Position.Companion.empty(), Team.empty(), User.empty(), new Date());
    }

    @Override
    public List<Item<Role>> asItems() {
        User user = getUser();
        return Arrays.asList(
                Item.Companion.text(holder.get(0), 0, Item.INPUT, R.string.first_name, user::getFirstName, user::setFirstName, this),
                Item.Companion.text(holder.get(1), 1, Item.INPUT, R.string.last_name, user::getLastName, user::setLastName, this),
                Item.Companion.text(holder.get(2), 2, Item.NICKNAME, R.string.nickname, this::getNickname, this::setNickname, this),
                Item.Companion.text(holder.get(3), 3, Item.ABOUT, R.string.user_about, user::getAbout, Item.Companion::ignore, this),
                Item.Companion.text(holder.get(4), 4, Item.ROLE, R.string.team_role, getPosition()::getCode, this::setPosition, this)
                        .textTransformer(value -> Config.positionFromCode(value.toString()).getName())
        );
    }

    @Override
    public Item<Role> getHeaderItem() {
        return Item.Companion.text(EMPTY_STRING, 0, Item.IMAGE, R.string.profile_picture, Item.Companion.nullToEmpty(getImageUrl()), this::setImageUrl, this);
    }

    @Override
    public boolean areContentsTheSame(Differentiable other) {
        if (!(other instanceof Role)) return getId().equals(other.getId());
        Role casted = (Role) other;
        return getPosition().equals(casted.getPosition())
                && getUser().areContentsTheSame(casted.getUser())
                && getTeam().areContentsTheSame(casted.getTeam());
    }

    @Override
    public Object getChangePayload(Differentiable other) {
        return other;
    }

    @Override
    public boolean isEmpty() {
        return TextUtils.isEmpty(getId());
    }

    @Override
    public void update(Role updated) {
        this.setId(updated.getId());
        this.setImageUrl(updated.getImageUrl());
        this.setNickname(updated.getNickname());
        this.getPosition().update(updated.getPosition());
        if (updated.getTeam().hasMajorFields()) this.getTeam().update(updated.getTeam());
        if (updated.getUser().hasMajorFields()) this.getUser().update(updated.getUser());
    }

    @Override
    public int compareTo(@NonNull Role o) {
        int roleComparison = getPosition().getCode().compareTo(o.getPosition().getCode());
        int userComparison = getUser().compareTo(o.getUser());
        int teamComparison = getTeam().compareTo(o.getTeam());

        return roleComparison != 0
                ? roleComparison
                : userComparison != 0
                ? userComparison
                : teamComparison != 0
                ? teamComparison
                : getId().compareTo(o.getId());
    }

    public boolean isPrivilegedRole() {
        String positionCode = getPosition().getCode();
        return !TextUtils.isEmpty(positionCode) && !isEmpty() && Config.getPrivileged().contains(positionCode);
    }

    public CharSequence getTitle() {
        CharSequence title = getUser().getFirstName();
        if (!TextUtils.isEmpty(getNickname())) return SpanBuilder.of(title)
                .appendNewLine().append("\"" + getNickname() + "\"")
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

            serialized.addProperty(ID_KEY, src.getId());
            serialized.addProperty(IMAGE_KEY, src.getImageUrl());
            serialized.addProperty(NICK_NAME_KEY, src.getNickname());
            serialized.add(USER_KEY, context.serialize(src.getUser()));

            String positionCode = src.getPosition().getCode();
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
