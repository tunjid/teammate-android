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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;

/**
 * Join request for a {@link Team}
 */

public class JoinRequest extends JoinRequestEntity
        implements
        UserHost,
        TeamHost,
        Model<JoinRequest>,
        HeaderedModel<JoinRequest>,
        ListableModel<JoinRequest> {

    public static final int INVITING = 0;
    public static final int JOINING = 1;
    public static final int APPROVING = 2;
    public static final int ACCEPTING = 3;
    public static final int WAITING = 4;

    private static final int ROLE_INDEX = 5;

    @Ignore private final List<Item<JoinRequest>> items;

    public static JoinRequest join(Team team, User user) {
        return new JoinRequest(false, true, "", Position.empty(), team, user, new Date());
    }

    public static JoinRequest invite(Team team) {
        return new JoinRequest(true, false, "", Position.empty(), team, User.empty(), new Date());
    }

    public JoinRequest(boolean teamApproved, boolean userApproved, String id, Position position, Team team, User user, Date created) {
        super(teamApproved, userApproved, id, position, team, user, created);
        items = buildItems();
    }

    protected JoinRequest(Parcel in) {
        super(in);
        items = buildItems();
    }

    @SuppressWarnings("unchecked")
    private List<Item<JoinRequest>> buildItems() {
        User user = getUser();
        return Arrays.asList(
                Item.text(0, Item.INPUT, R.string.first_name, user::getFirstName, user::setFirstName, this),
                Item.text(1, Item.INPUT, R.string.last_name, user::getLastName, user::setLastName, this),
                Item.text(3, Item.INPUT, R.string.user_about, user::getAbout, Item::ignore, this),
                Item.email(4, Item.INPUT, R.string.email, user::getPrimaryEmail, user::setPrimaryEmail, this),
                // END USER ITEMS
                Item.text(ROLE_INDEX, Item.ROLE, R.string.team_role, position::getCode, this::setPosition, this)
                        .textTransformer(value -> Config.positionFromCode(value.toString()).getName()),
                // START TEAM ITEMS
                Item.text(6, Item.INPUT, R.string.team_name, team::getName, Item::ignore, this),
                Item.text(7, Item.SPORT, R.string.team_sport, team.getSport()::getCode, Item::ignore, this).textTransformer(value -> Config.sportFromCode(value.toString()).getName()),
                Item.text(8, Item.CITY, R.string.city, team::getCity, Item::ignore, this),
                Item.text(9, Item.STATE, R.string.state, team::getState, Item::ignore, this),
                Item.text(10, Item.ZIP, R.string.zip, team::getZip, Item::ignore, this),
                Item.text(11, Item.DESCRIPTION, R.string.team_description, team::getDescription, Item::ignore, this),
                Item.number(12, Item.NUMBER, R.string.team_min_age, () -> String.valueOf(team.getMinAge()), Item::ignore, this),
                Item.number(13, Item.NUMBER, R.string.team_max_age, () -> String.valueOf(team.getMaxAge()), Item::ignore, this)
        );
    }

    @Override
    public List<Item<JoinRequest>> asItems() {
        List<Item<JoinRequest>> filtered = new ArrayList<>();
        Flowable.fromIterable(items)
                .filter(this::filter)
                .collectInto(filtered, List::add)
                .subscribe();
        return filtered;
    }

    @Override
    public Item<JoinRequest> getHeaderItem() {
        return Item.text(0, Item.IMAGE, R.string.profile_picture, user::getImageUrl, imageUrl -> {}, this);
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
        return TextUtils.isEmpty(id);
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
        restItemList();
    }

    @Override
    public void update(JoinRequest updated) {
        this.teamApproved = updated.teamApproved;
        this.userApproved = updated.userApproved;
        this.id = updated.id;

        position.update(updated.position);
        team.update(updated.team);
        user.update(updated.user);
        updateItemList(updated);
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

    private boolean filter(Item<JoinRequest> item) {
        boolean isEmpty = isEmpty();
        int sortPosition = item.getSortPosition();

        // Joining a team
        if (userApproved) return sortPosition >= ROLE_INDEX;

        // Inviting a user
        boolean ignoreTeam = sortPosition <= ROLE_INDEX;

        int stringRes = item.getStringRes();

        return isEmpty
                ? ignoreTeam && stringRes != R.string.user_about
                : ignoreTeam && stringRes != R.string.email;
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
            Date created = ModelUtils.parseDate(ModelUtils.asString(CREATED_KEY, requestJson));

            if (team == null) team = Team.empty();
            if (user == null) user = User.empty();

            return new JoinRequest(teamApproved, userApproved, id, position, team, user, created);
        }
    }
}
