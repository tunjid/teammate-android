package com.mainstreetcode.teammates.model;

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
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.notifications.Notifiable;
import com.mainstreetcode.teammates.notifications.Notifier;
import com.mainstreetcode.teammates.notifications.RoleNotifier;
import com.mainstreetcode.teammates.persistence.entity.RoleEntity;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * Roles on a team
 */

public class Role extends RoleEntity
        implements
        Model<Role>,
        Notifiable<Role>,
        HeaderedModel<Role>,
        ItemListableBean<Role> {

    public static final String PHOTO_UPLOAD_KEY = "role-photo";
    private static final List<String> PRIVILEGED_ROLES = Arrays.asList("Admin", "Coach", "Assistant Coach");

    @Ignore private final List<Item<Role>> items;

    @SuppressWarnings("unused")
    public Role(String id, String name, String imageUrl, Team team, User user) {
        super(id, name, imageUrl, team, user);
        items = buildItems();
    }

    protected Role(Parcel in) {
        super(in);
        items = buildItems();
    }

    public static Role empty() {
        return new Role("", "", "", Team.empty(), User.empty());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Item<Role>> buildItems() {
        User user = getUser();
        return Arrays.asList(
                new Item(Item.INPUT, R.string.first_name, R.string.user_info, user.getFirstName() == null ? "" : user.getFirstName(), user::setFirstName, this),
                new Item(Item.INPUT, R.string.last_name, user.getLastName() == null ? "" : user.getLastName(), user::setLastName, this),
                //new Item(Item.INPUT, R.string.email, user.getPrimaryEmail() == null ? "" : user.getPrimaryEmail(), user::setPrimaryEmail, this),
                new Item(Item.ROLE, R.string.team_role, R.string.team_role, name, this::setName, this)
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
    public Item<Role> getHeaderItem() {
        return new Item<>(Item.IMAGE, R.string.profile_picture, R.string.profile_picture, imageUrl, this::setImageUrl, this);
    }

    @Override
    public boolean areContentsTheSame(Identifiable other) {
        if (!(other instanceof Role)) return id.equals(other.getId());
        Role casted = (Role) other;
        return name.equals(casted.name) && user.areContentsTheSame(casted.getUser());
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
    public void reset() {
        name = "";
        imageUrl = "";
        team.reset();
        user.reset();
    }

    @Override
    public void update(Role updated) {
        this.id = updated.getId();
        this.name = updated.name;
        this.imageUrl = updated.imageUrl;

        int size = size();
        for (int i = 0; i < size; i++) get(i).setValue(updated.get(i).getValue());

        this.team.update(updated.team);
        this.user.update(updated.user);
    }

    @Override
    public int compareTo(@NonNull Role o) {
        int roleComparison = name.compareTo(o.name);
        int userComparison = user.compareTo(o.user);

        return roleComparison != 0
                ? roleComparison
                : userComparison != 0
                ? userComparison
                : id.compareTo(o.id);
    }

    @Override
    public Notifier<Role> getNotifier() {
        return RoleNotifier.getInstance();
    }

    public boolean isPrivilegedRole() {
        return !TextUtils.isEmpty(name) && !isEmpty() && PRIVILEGED_ROLES.contains(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    @SuppressWarnings("unused")
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

        @Override
        public JsonElement serialize(Role src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject serialized = new JsonObject();

            serialized.addProperty(ID_KEY, src.getId());
            serialized.addProperty(NAME_KEY, src.getName());

            serialized.add(USER_KEY, context.serialize(src.user));

            return serialized;
        }

        @Override
        public Role deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonObject roleJson = json.getAsJsonObject();

            String id = ModelUtils.asString(ID_KEY, roleJson);
            String name = ModelUtils.asString(NAME_KEY, roleJson);
            String imageUrl = ModelUtils.asString(IMAGE_KEY, roleJson);
            Team team = context.deserialize(roleJson.get(TEAM_KEY), Team.class);
            User user = context.deserialize(roleJson.get(USER_KEY), User.class);

            if (user == null) user = User.empty();

            return new Role(id, name, imageUrl, team, user);
        }
    }
}
