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

import androidx.room.Ignore;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
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
import com.mainstreetcode.teammate.persistence.entity.JoinRequestEntity;
import com.mainstreetcode.teammate.util.IdCache;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.mainstreetcode.teammate.util.ModelUtils.EMPTY_STRING;

/**
 * Join request for a {@link Team}
 */

public class JoinRequest extends JoinRequestEntity
        implements
        UserHost,
        TeamHost,
        HeaderedModel<JoinRequest>,
        ListableModel<JoinRequest>,
        TeamMemberModel<JoinRequest> {

    @Ignore private static final IdCache holder = IdCache.cache(13);

    @NonNull
    private static Team copyTeam(Team team) {
        Team copy = Team.empty();
        copy.update(team);
        return copy;
    }

    public static JoinRequest join(Team team, User user) {
        return new JoinRequest(false, true, "", Config.positionFromCode(""), copyTeam(team), user, new Date());
    }

    public static JoinRequest invite(Team team) {
        return new JoinRequest(true, false, "", Config.positionFromCode(""), copyTeam(team), User.empty(), new Date());
    }

    public JoinRequest(boolean teamApproved, boolean userApproved, String id, Position position, Team team, User user, Date created) {
        super(teamApproved, userApproved, id, position, team, user, created);
    }

    protected JoinRequest(Parcel in) {
        super(in);
    }

    @Override
    public List<Item<JoinRequest>> asItems() {
        User user = getUser();
        return Arrays.asList(
                Item.Companion.text(holder.get(0), 0, Item.INPUT, R.string.first_name, user::getFirstName, user::setFirstName, this),
                Item.Companion.text(holder.get(1), 1, Item.INPUT, R.string.last_name, user::getLastName, user::setLastName, this),
                Item.Companion.text(holder.get(2), 2, Item.ABOUT, R.string.user_about, user::getAbout, Item.Companion::ignore, this),
                Item.Companion.email(holder.get(3), 3, Item.INPUT, R.string.email, user::getPrimaryEmail, user::setPrimaryEmail, this),
                // END USER ITEMS
                Item.Companion.text(holder.get(4),4, Item.ROLE, R.string.team_role, getPosition()::getCode, this::setPosition, this)
                        .textTransformer(value -> Config.positionFromCode(value.toString()).getName()),
                // START TEAM ITEMS
                Item.Companion.text(holder.get(5), 5, Item.INPUT, R.string.team_name, getTeam()::getName, Item.Companion::ignore, this),
                Item.Companion.text(holder.get(6), 6, Item.SPORT, R.string.team_sport, getTeam().getSport()::getCode, Item.Companion::ignore, this).textTransformer(value -> Config.sportFromCode(value.toString()).getName()),
                Item.Companion.text(holder.get(7), 7, Item.CITY, R.string.city, getTeam()::getCity, Item.Companion::ignore, this),
                Item.Companion.text(holder.get(8), 8, Item.STATE, R.string.state, getTeam()::getState, Item.Companion::ignore, this),
                Item.Companion.text(holder.get(9), 9, Item.ZIP, R.string.zip, getTeam()::getZip, Item.Companion::ignore, this),
                Item.Companion.text(holder.get(10), 10, Item.DESCRIPTION, R.string.team_description, getTeam()::getDescription, Item.Companion::ignore, this),
                Item.Companion.number(holder.get(11), 11, Item.NUMBER, R.string.team_min_age, () -> String.valueOf(getTeam().getMinAge()), Item.Companion::ignore, this),
                Item.Companion.number(holder.get(12), 12, Item.NUMBER, R.string.team_max_age, () -> String.valueOf(getTeam().getMaxAge()), Item.Companion::ignore, this)
        );
    }

    @Override
    public Item<JoinRequest> getHeaderItem() {
        RemoteImage image = isUserApproved() ? getTeam() : getUser();
        return Item.Companion.text(EMPTY_STRING, 0, Item.IMAGE, R.string.profile_picture, image::getImageUrl, imageUrl -> {}, this);
    }

    @Override
    public boolean areContentsTheSame(Differentiable other) {
        if (!(other instanceof JoinRequest)) return getId().equals(other.getId());
        JoinRequest casted = (JoinRequest) other;
        return getPosition().equals(casted.getPosition()) && getUser().areContentsTheSame(casted.getUser());
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
    public String getImageUrl() {
        return getUser() == null ? null : getUser().getImageUrl();
    }


    @Override
    public void update(JoinRequest updated) {
        this.setTeamApproved(updated.isTeamApproved());
        this.setUserApproved(updated.isUserApproved());
        this.setId(updated.getId());

        getPosition().update(updated.getPosition());
        if (updated.getTeam().hasMajorFields()) getTeam().update(updated.getTeam());
        if (updated.getUser().hasMajorFields()) getUser().update(updated.getUser());
    }

    @Override
    public int compareTo(@NonNull JoinRequest o) {
        int roleComparison = getPosition().getCode().compareTo(o.getPosition().getCode());
        int userComparison = getUser().compareTo(o.getUser());

        return roleComparison != 0
                ? roleComparison
                : userComparison != 0
                ? userComparison
                : getId().compareTo(o.getId());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public static final Parcelable.Creator<JoinRequest> CREATOR = new Parcelable.Creator<JoinRequest>() {
        @Override
        public JoinRequest createFromParcel(Parcel in) {
            return new JoinRequest(in);
        }

        @Override
        public JoinRequest[] newArray(int size) {
            return new JoinRequest[size];
        }
    };

    public static class GsonAdapter
            implements
            JsonSerializer<JoinRequest>,
            JsonDeserializer<JoinRequest> {

        private static final String ID_KEY = "_id";
        private static final String NAME_KEY = "roleName";
        private static final String USER_KEY = "user";
        private static final String USER_FIRST_NAME_KEY = "firstName";
        private static final String USER_LAST_NAME_KEY = "lastName";
        private static final String USER_PRIMARY_EMAIL_KEY = "primaryEmail";
        private static final String TEAM_KEY = "team";
        private static final String TEAM_APPROVAL_KEY = "teamApproved";
        private static final String USER_APPROVAL_KEY = "userApproved";
        private static final String CREATED_KEY = "created";

        @Override
        public JsonElement serialize(JoinRequest src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();

            result.addProperty(TEAM_KEY, src.getTeam().getId());
            result.addProperty(TEAM_APPROVAL_KEY, src.isTeamApproved());
            result.addProperty(USER_APPROVAL_KEY, src.isUserApproved());

            User user = src.getUser();

            if (src.isTeamApproved()) {
                result.addProperty(USER_FIRST_NAME_KEY, user.getFirstName().toString());
                result.addProperty(USER_LAST_NAME_KEY, user.getLastName().toString());
                result.addProperty(USER_PRIMARY_EMAIL_KEY, user.getPrimaryEmail());
            }
            else {
                result.addProperty(USER_KEY, src.getUser().getId());
            }

            String positionCode = src.getPosition().getCode();
            if (!TextUtils.isEmpty(positionCode)) result.addProperty(NAME_KEY, positionCode);

            return result;
        }

        @Override
        public JoinRequest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonObject requestJson = json.getAsJsonObject();

            boolean teamApproved = ModelUtils.asBoolean(TEAM_APPROVAL_KEY, requestJson);
            boolean userApproved = ModelUtils.asBoolean(USER_APPROVAL_KEY, requestJson);

            String id = ModelUtils.asString(ID_KEY, requestJson);
            String positionName = ModelUtils.asString(NAME_KEY, requestJson);
            Position position = Config.positionFromCode(positionName);

            Team team = context.deserialize(requestJson.get(TEAM_KEY), Team.class);
            User user = context.deserialize(requestJson.get(USER_KEY), User.class);
            Date created = ModelUtils.parseDate(ModelUtils.asString(CREATED_KEY, requestJson));

            if (team == null) team = Team.empty();
            if (user == null) user = User.empty();

            return new JoinRequest(teamApproved, userApproved, id, position, team, user, created);
        }
    }
}
