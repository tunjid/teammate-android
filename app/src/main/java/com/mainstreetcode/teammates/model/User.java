package com.mainstreetcode.teammates.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import static com.mainstreetcode.teammates.model.ModelUtils.asBoolean;
import static com.mainstreetcode.teammates.model.ModelUtils.asString;

/**
 * Users that may be part of a {@link Team}
 * <p>
 * Created by Shemanigans on 6/4/17.
 */

@Entity(tableName = "users")
public class User {

    @PrimaryKey
    private String id;
    private String firstName;
    private String lastName;
    private String primaryEmail;

    @Ignore private transient String role;
    @Ignore private transient String password;
    @Ignore private transient boolean isTeamApproved;

    public void setPassword(String password) {
        this.password = password;
    }

    public User(String id, String firstName, String lastName, String primaryEmail) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.primaryEmail = primaryEmail;
    }

    public static class JsonDeserializer implements com.google.gson.JsonDeserializer<User> {

        private static final String UID_KEY = "_id";
        private static final String ROLE_KEY = "role";
        private static final String LAST_NAME_KEY = "lastName";
        private static final String FIRST_NAME_KEY = "firstName";
        private static final String PRIMARY_EMAIL_KEY = "primaryEmail";
        private static final String TEAM_APPROVED_KEY = "isTeamApproved";

        @Override
        public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject userObject = json.getAsJsonObject();

            String id = asString(UID_KEY, userObject);
            String firstName = asString(FIRST_NAME_KEY, userObject);
            String lastName = asString(LAST_NAME_KEY, userObject);
            String primaryEmail = asString(PRIMARY_EMAIL_KEY, userObject);

            User user = new User(id, firstName, lastName, primaryEmail);

            user.setRole(asString(ROLE_KEY, userObject));
            user.setTeamApproved(asBoolean(TEAM_APPROVED_KEY, userObject));

            return user;
        }
    }

    @Override
    public String toString() {return "com.mainstreetcode.teammates.model.User(id=" + this.id + ", firstName=" + this.firstName + ", lastName=" + this.lastName + ", primaryEmail=" + this.primaryEmail + ")";}

    public String getId() {return this.id;}

    public String getFirstName() {return this.firstName;}

    @SuppressWarnings("unused")
    public String getLastName() {return this.lastName;}

    public String getPrimaryEmail() {return this.primaryEmail;}

    public String getRole() {
        return role;
    }

    private void setRole(String role) {
        this.role = role;
    }

    public boolean isTeamApproved() {
        return isTeamApproved;
    }

    private void setTeamApproved(boolean teamApproved) {
        isTeamApproved = teamApproved;
    }
}
