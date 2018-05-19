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
import com.mainstreetcode.teammate.persistence.entity.UserEntity;
import com.mainstreetcode.teammate.util.IdCache;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import static com.mainstreetcode.teammate.util.ModelUtils.EMPTY_STRING;
import static com.mainstreetcode.teammate.util.ModelUtils.areNotEmpty;
import static com.mainstreetcode.teammate.util.ModelUtils.asString;

public class User extends UserEntity implements
        Model<User>,
        HeaderedModel<User>,
        ListableModel<User> {

    public static final String PHOTO_UPLOAD_KEY = "user-photo";

    @Ignore private transient String password;
    @Ignore private static final IdCache holder = IdCache.cache(4);

    public User(String id, String imageUrl, String primaryEmail, CharSequence firstName, CharSequence lastName, CharSequence about) {
        super(id, imageUrl, primaryEmail, firstName, lastName, about);
    }

    protected User(Parcel in) {
        super(in);
    }

    public static User empty() {
        return new User("", Config.getDefaultUserAvatar(), "", "", "", "");
    }

    @Override
    public List<Item<User>> asItems() {
        return Arrays.asList(
                Item.text(holder.get(0), 0, Item.INPUT, R.string.first_name, Item.nullToEmpty(firstName), this::setFirstName, this),
                Item.text(holder.get(1), 1, Item.INPUT, R.string.last_name, Item.nullToEmpty(lastName), this::setLastName, this),
                Item.email(holder.get(2), 2, Item.INPUT, R.string.email, Item.nullToEmpty(primaryEmail), this::setPrimaryEmail, this),
                Item.text(holder.get(3), 3, Item.ABOUT, R.string.user_about, Item.nullToEmpty(about), this::setAbout, this)
        );
    }

    @Override
    public Item<User> getHeaderItem() {
        return Item.text(EMPTY_STRING, 0, Item.IMAGE, R.string.profile_picture, Item.nullToEmpty(imageUrl), this::setImageUrl, this);
    }

    @Override
    public boolean areContentsTheSame(Identifiable other) {
        if (!(other instanceof User)) return id.equals(other.getId());
        User casted = (User) other;
        return firstName.equals(casted.getFirstName()) && lastName.equals(casted.getLastName())
                && imageUrl.equals(casted.getImageUrl());
    }

    @Override
    public boolean hasMajorFields() {
        return areNotEmpty(id, firstName, lastName);
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
    public void update(User updatedUser) {
        this.id = updatedUser.id;
        this.about = updatedUser.about;
        this.firstName = updatedUser.firstName;
        this.lastName = updatedUser.lastName;
        this.imageUrl = updatedUser.imageUrl;
        this.primaryEmail = updatedUser.primaryEmail;
    }

    @Override
    public int compareTo(@NonNull User o) {
        int firstNameComparison = firstName.toString().compareTo(o.firstName.toString());
        int lastNameComparison = lastName.toString().compareTo(o.lastName.toString());

        return firstNameComparison != 0
                ? firstNameComparison
                : lastNameComparison != 0
                ? lastNameComparison
                : id.compareTo(o.id);
    }

    public void setPassword(String password) {
        this.password = password;
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

    public static class GsonAdapter implements
            JsonSerializer<User>,
            JsonDeserializer<User> {

        private static final String UID_KEY = "_id";
        private static final String LAST_NAME_KEY = "lastName";
        private static final String FIRST_NAME_KEY = "firstName";
        private static final String IMAGE_KEY = "imageUrl";
        private static final String PRIMARY_EMAIL_KEY = "primaryEmail";
        private static final String ABOUT_KEY = "about";
        private static final String PASSWORD_KEY = "password";

        @Override
        public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) {
                return new User(json.getAsString(), "", "", "", "", "");
            }

            JsonObject userObject = json.getAsJsonObject();

            String id = asString(UID_KEY, userObject);
            String imageUrl = asString(IMAGE_KEY, userObject);
            String primaryEmail = asString(PRIMARY_EMAIL_KEY, userObject);
            String firstName = asString(FIRST_NAME_KEY, userObject);
            String lastName = asString(LAST_NAME_KEY, userObject);
            String about = asString(ABOUT_KEY, userObject);

            return new User(id, imageUrl, primaryEmail, firstName, lastName, about);
        }

        @Override
        public JsonElement serialize(User src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject user = new JsonObject();
            user.addProperty(FIRST_NAME_KEY, src.firstName.toString());
            user.addProperty(LAST_NAME_KEY, src.lastName.toString());
            user.addProperty(PRIMARY_EMAIL_KEY, src.primaryEmail);
            user.addProperty(ABOUT_KEY, src.about.toString());

            if (!TextUtils.isEmpty(src.password)) user.addProperty(PASSWORD_KEY, src.password);

            return user;
        }
    }
}
