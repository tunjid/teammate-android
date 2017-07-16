package com.mainstreetcode.teammates.model;

import android.arch.persistence.room.Ignore;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.persistence.entity.UserEntity;
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

public class User extends UserEntity implements
        ListableBean<User, Item> {

    public static final int IMAGE_POSITION = 0;
    public static final int EMAIL_POSITION = 3;
    //private static final int ROLE_POSITION = 4;

    @Ignore private transient String password;

    @Ignore private final List<Item<User>> items;

    public User(String id, String firstName, String lastName, String primaryEmail, String imageUrl) {
        super(id, firstName, lastName, primaryEmail, imageUrl);

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

        @Override
        public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject userObject = json.getAsJsonObject();

            String id = asString(UID_KEY, userObject);
            String firstName = asString(FIRST_NAME_KEY, userObject);
            String lastName = asString(LAST_NAME_KEY, userObject);
            String primaryEmail = asString(PRIMARY_EMAIL_KEY, userObject);
            String imageUrl = TeammateService.API_BASE_URL + asString(IMAGE_KEY, userObject);

            return new User(id, firstName, lastName, primaryEmail, imageUrl);
        }

        @Override
        public JsonElement serialize(User src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject user = new JsonObject();
            //user.addProperty(UID_KEY, src.id);
            user.addProperty(FIRST_NAME_KEY, src.firstName);
            user.addProperty(LAST_NAME_KEY, src.lastName);
            user.addProperty(PRIMARY_EMAIL_KEY, src.primaryEmail);
            user.addProperty(PASSWORD_KEY, src.password);

            return user;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Item<User>> itemsFromUser(User user) {
        String imageUrl =  user.imageUrl;
        return Arrays.asList(
                new Item(Item.IMAGE, R.string.profile_picture, R.string.profile_picture, imageUrl, null, User.class),
                new Item(Item.INPUT, R.string.first_name, R.string.user_info, user.firstName == null ? "" : user.firstName, user::setFirstName, User.class),
                new Item(Item.INPUT, R.string.last_name, user.lastName == null ? "" : user.lastName, user::setLastName, User.class),
                new Item(Item.INPUT, R.string.email, user.primaryEmail == null ? "" : user.primaryEmail, user::setPrimaryEmail, User.class)
                //new Item(Item.ROLE, R.string.team_role, R.string.team_role, user.role == null ? "" : user.role.getName(), user::setRoleName, User.class)
        );
    }

    public void update(User updatedUser) {
        int size = size();
        for (int i = 0; i < size; i++) get(i).setValue(updatedUser.get(i).getValue());
    }


    public void setPassword(String password) {
        this.password = password;
    }



    protected User(Parcel in) {
        super(in);
        items = itemsFromUser(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
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
