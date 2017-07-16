package com.mainstreetcode.teammates.model;

import android.arch.persistence.room.Ignore;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.persistence.entity.RoleEntity;
import com.mainstreetcode.teammates.rest.TeammateService;
import com.mainstreetcode.teammates.util.ListableBean;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * Roles on a team
 * <p>
 * Created by Shemanigans on 6/6/17.
 */

public class Role extends RoleEntity
        implements
        ListableBean<Role, Item> {

    public static final String PHOTO_UPLOAD_KEY = "role-photo";
    private static final String ADMIN = "Admin";
    public static final int IMAGE_POSITION = 0;

    @Ignore private final List<Item<Role>> items;

    @SuppressWarnings("unused")
    public Role(String id, String name, String teamId, String imageUrl, User user) {
        super(id, name, teamId, imageUrl, user);
        items = itemsFromRole(this);
    }

    protected Role(Parcel in) {
        super(in);
        items = itemsFromRole(this);
    }

    public static class GsonAdapter
            implements
            JsonDeserializer<Role> {

        private static final String ID_KEY = "_id";
        private static final String NAME_KEY = "name";
        private static final String USER_KEY = "user";
        private static final String TEAM_KEY = "team";
        private static final String IMAGE_KEY = "imageUrl";

        @Override
        public Role deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonObject roleJson = json.getAsJsonObject();

            String id = ModelUtils.asString(ID_KEY, roleJson);
            String name = ModelUtils.asString(NAME_KEY, roleJson);
            String teamId = ModelUtils.asString(TEAM_KEY, roleJson);
            String imageUrl = TeammateService.API_BASE_URL + ModelUtils.asString(IMAGE_KEY, roleJson);
            User user = context.deserialize(roleJson.get(USER_KEY), User.class);

            return new Role(id, name, teamId, imageUrl, user);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Item<Role>> itemsFromRole(Role role) {
        User user = role.getUser();
        String imageUrl =  role.imageUrl;
        return Arrays.asList(
                new Item(Item.IMAGE, R.string.profile_picture, R.string.profile_picture, imageUrl, null, Role.class),
                new Item(Item.INPUT, R.string.first_name, R.string.user_info, user.getFirstName() == null ? "" : user.getFirstName(), user::setFirstName, Role.class),
                new Item(Item.INPUT, R.string.last_name, user.getLastName() == null ? "" : user.getLastName(), user::setLastName, Role.class),
                new Item(Item.INPUT, R.string.email, user.getPrimaryEmail() == null ? "" : user.getPrimaryEmail(), user::setPrimaryEmail, Role.class),
                new Item(Item.ROLE, R.string.team_role, R.string.team_role, role.getName(), role::setName, User.class)
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
    public Role toSource() {
        return null;
    }

    public boolean isTeamAdmin() {
        return !TextUtils.isEmpty(name) && ADMIN.equals(name);
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
}
