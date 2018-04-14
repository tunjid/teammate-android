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
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.enums.Position;
import com.mainstreetcode.teammate.persistence.entity.JoinRequestEntity;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * Join request for a {@link Team}
 */

public class JoinRequest extends JoinRequestEntity
        implements
        Model<JoinRequest>,
        HeaderedModel<JoinRequest>,
        ItemListableBean<JoinRequest> {

    @Ignore private final List<Item<JoinRequest>> items;

    public static JoinRequest join(Position position, Team team, User user) {
        return new JoinRequest(false, true, "", position, team, user);
    }

    public static JoinRequest invite(Team team) {
        return new JoinRequest(true, false, "", Position.empty(), team, User.empty());
    }

    public JoinRequest(boolean teamApproved, boolean userApproved, String id, Position position, Team team, User user) {
        super(teamApproved, userApproved, id, position, team, user);
        items = buildItems();
    }

    protected JoinRequest(Parcel in) {
        super(in);
        items = buildItems();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Item<JoinRequest>> buildItems() {
        User user = getUser();
        return Arrays.asList(
                Item.text(Item.INPUT, R.string.first_name, user.getFirstName() == null ? "" : user.getFirstName(), user::setFirstName, this),
                Item.text(Item.INPUT, R.string.last_name, user.getLastName() == null ? "" : user.getLastName(), user::setLastName, this),
                Item.email(Item.INPUT, R.string.email, user.getPrimaryEmail() == null ? "" : user.getPrimaryEmail(), user::setPrimaryEmail, this),
                Item.text(Item.ROLE, R.string.team_role, position != null ? position.getName() : "", this::setPosition, this)
        );
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public Item get(int position) {
        return items.get(position);
    }

    @Override
    public Item<JoinRequest> getHeaderItem() {
        return Item.text(Item.IMAGE, R.string.profile_picture, user.getImageUrl(), imageUrl -> {}, this);
    }

    @Override
    public boolean areContentsTheSame(Identifiable other) {
        if (!(other instanceof JoinRequest)) return id.equals(other.getId());
        JoinRequest casted = (JoinRequest) other;
        return position.equals(casted.position) && user.areContentsTheSame(casted.getUser());
    }

    @Override
    public Object getChangePayload(Identifiable other) {
        return other;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public String getImageUrl() {
        return user == null ? null : user.getImageUrl();
    }

    @Override
    public void reset() {
        userApproved = false;
        teamApproved = false;
        position.reset();
        user.reset();
        team.reset();
    }

    @Override
    public void update(JoinRequest updated) {
        this.teamApproved = updated.teamApproved;
        this.userApproved = updated.userApproved;
        this.id = updated.id;

        position.update(updated.position);
        team.update(updated.team);
        user.update(updated.user);
    }

    @Override
    public int compareTo(@NonNull JoinRequest o) {
        int roleComparison = position.getCode().compareTo(o.position.getCode());
        int userComparison = user.compareTo(o.user);

        return roleComparison != 0
                ? roleComparison
                : userComparison != 0
                ? userComparison
                : id.compareTo(o.id);
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

        @Override
        public JsonElement serialize(JoinRequest src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();

            result.addProperty(TEAM_KEY, src.team.getId());
            result.addProperty(TEAM_APPROVAL_KEY, src.teamApproved);
            result.addProperty(USER_APPROVAL_KEY, src.userApproved);

            User user = src.user;

            if (src.teamApproved) {
                result.addProperty(USER_FIRST_NAME_KEY, user.getFirstName());
                result.addProperty(USER_LAST_NAME_KEY, user.getLastName());
                result.addProperty(USER_PRIMARY_EMAIL_KEY, user.getPrimaryEmail());
            }
            else {
                result.addProperty(USER_KEY, src.getUser().getId());
            }

            String positionCode = src.position != null ? src.position.getCode() : "";
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

            if (team == null) team = Team.empty();
            if (user == null) user = User.empty();

            return new JoinRequest(teamApproved, userApproved, id, position, team, user);
        }
    }
}
