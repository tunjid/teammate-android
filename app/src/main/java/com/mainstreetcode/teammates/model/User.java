package com.mainstreetcode.teammates.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
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
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.rest.TeammateService;
import com.mainstreetcode.teammates.util.ListableBean;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import static com.mainstreetcode.teammates.model.ModelUtils.asString;

/**
 * Users that may be part of a {@link Team}
 * <p>
 * Created by Shemanigans on 6/4/17.
 */

@Entity(tableName = "users")
public class User implements
        Parcelable,
        ListableBean<User, Item> {

    public static final int IMAGE_POSITION = 0;
    public static final int EMAIL_POSITION = 3;
    //private static final int ROLE_POSITION = 4;

    @PrimaryKey @ColumnInfo(name = "user_id") private String id;
    @ColumnInfo(name = "user_first_name") private String firstName;
    @ColumnInfo(name = "user_last_name") private String lastName;
    @ColumnInfo(name = "user_primary_email") private String primaryEmail;
    @ColumnInfo(name = "user_image_url") private String imageUrl;

    @Ignore private transient String password;

    @Ignore private transient Role role;
    @Ignore private transient JoinRequest request;

    @Ignore private final List<Item<User>> items;

    public User(String id, String firstName, String lastName, String primaryEmail, String imageUrl) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.imageUrl = imageUrl;
        this.primaryEmail = primaryEmail;

        items = itemsFromUser(this);
    }

    public User(String id, String firstName, String lastName, String primaryEmail, String imageUrl,
                Role role, JoinRequest request) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.imageUrl = imageUrl;
        this.primaryEmail = primaryEmail;
        this.role = role;
        this.request = request;

        items = itemsFromUser(this);
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
    public User toSource() {
        return null;
    }

    public static class GsonAdapter implements
            JsonSerializer<User>,
            JsonDeserializer<User> {

        private static final String UID_KEY = "_id";
        private static final String PASSWORD_KEY = "password";
        private static final String LAST_NAME_KEY = "lastName";
        private static final String FIRST_NAME_KEY = "firstName";
        private static final String IMAGE_KEY = "imageUrl";
        private static final String PRIMARY_EMAIL_KEY = "primaryEmail";
        private static final String ROLE_KEY = "role";
        private static final String JOIN_REQUEST_KEY = "joinRequest";

        @Override
        public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject userObject = json.getAsJsonObject();

            String id = asString(UID_KEY, userObject);
            String firstName = asString(FIRST_NAME_KEY, userObject);
            String lastName = asString(LAST_NAME_KEY, userObject);
            String primaryEmail = asString(PRIMARY_EMAIL_KEY, userObject);
            String imageUrl = TeammateService.API_BASE_URL + asString(IMAGE_KEY, userObject);

            JsonElement roleJson = userObject.get(ROLE_KEY);
            JsonElement joinRequestJson = userObject.get(JOIN_REQUEST_KEY);

            Role role = context.deserialize(roleJson, Role.class);
            JoinRequest request = context.deserialize(joinRequestJson, JoinRequest.class);

            return new User(id, firstName, lastName, primaryEmail, imageUrl, role, request);
        }

        @Override
        public JsonElement serialize(User src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject user = new JsonObject();
            //user.addProperty(UID_KEY, src.id);
            user.addProperty(FIRST_NAME_KEY, src.firstName);
            user.addProperty(LAST_NAME_KEY, src.lastName);
            user.addProperty(PRIMARY_EMAIL_KEY, src.primaryEmail);
            user.addProperty(PASSWORD_KEY, src.password);

            if (src.role != null && !TextUtils.isEmpty(src.role.getName())) {
                user.addProperty(ROLE_KEY, src.role.getName());
            }

            return user;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Item<User>> itemsFromUser(User user) {
        String imageUrl = user.role != null ? user.role.getImageUrl() : user.imageUrl;
        return Arrays.asList(
                new Item(Item.IMAGE, R.string.profile_picture, R.string.profile_picture, imageUrl, null, User.class),
                new Item(Item.INPUT, R.string.first_name, R.string.user_info, user.firstName == null ? "" : user.firstName, user::setFirstName, User.class),
                new Item(Item.INPUT, R.string.last_name, user.lastName == null ? "" : user.lastName, user::setLastName, User.class),
                new Item(Item.INPUT, R.string.email, user.primaryEmail == null ? "" : user.primaryEmail, user::setPrimaryEmail, User.class),
                new Item(Item.ROLE, R.string.team_role, R.string.team_role, user.role == null ? "" : user.role.getName(), user::setRoleName, User.class)
        );
    }

    public void update(User updatedUser) {
        int size = size();
        for (int i = 0; i < size; i++) get(i).setValue(updatedUser.get(i).getValue());
        this.role = updatedUser.role;
        this.request = updatedUser.request;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {return "com.mainstreetcode.teammates.model.User(id=" + this.id + ", firstName=" + this.firstName + ", lastName=" + this.lastName + ", primaryEmail=" + this.primaryEmail + ")";}

    public String getId() {return this.id;}

    public String getFirstName() {return this.firstName;}

    @SuppressWarnings("unused")
    public String getLastName() {return this.lastName;}

    public String getPrimaryEmail() {return this.primaryEmail;}

    public String getImageUrl() {
        return imageUrl;
    }

    public String getRoleName() {
        return role == null ? "" : role.getName();
    }

    public Role getRole() {
        return role;
    }

    public boolean isTeamApproved() {
        return request != null && request.isTeamApproved();
    }

    public boolean isUserApproved() {
        return request != null && request.isUserApproved();
    }

    private void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    private void setLastName(String lastName) {
        this.lastName = lastName;
    }

    private void setPrimaryEmail(String primaryEmail) {
        this.primaryEmail = primaryEmail;
    }

    public void setPassword(String password) {
        this.password = password;
    }

//    private void setImageUrl(String imageUrl) {
//        this.imageUrl = imageUrl;
//    }

    private void setRoleName(String roleName) {
        if (this.role != null) this.role.setName(roleName);
    }

    protected User(Parcel in) {
        id = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        primaryEmail = in.readString();
        imageUrl = in.readString();
        role = (Role) in.readValue(Role.class.getClassLoader());
        items = itemsFromUser(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(primaryEmail);
        dest.writeString(imageUrl);
        dest.writeValue(role);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
