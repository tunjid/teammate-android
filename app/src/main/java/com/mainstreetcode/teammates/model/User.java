package com.mainstreetcode.teammates.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import static com.mainstreetcode.teammates.model.Team.JsonDeserializer.asString;

/**
 * Users that may be part of a {@link Team}
 * <p>
 * Created by Shemanigans on 6/4/17.
 */

@Entity(tableName = "users")
public class User {

    @PrimaryKey
    public String id;
    public String firstName;
    public String lastName;
    public String primaryEmail;

    @Ignore
    public String password;

    public User(String id, String firstName, String lastName, String primaryEmail) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.primaryEmail = primaryEmail;
    }

    public User(String id, String firstName, String lastName, String primaryEmail, String password) {
        this(id, firstName, lastName, primaryEmail);
        this.password = password;
    }

    public static class JsonDeserializer implements com.google.gson.JsonDeserializer<User> {

        private static final String UID_KEY = "_id";
        private static final String FIRST_NAME_KEY = "firstName";
        private static final String LAST_NAME_KEY = "lastName";
        private static final String PRIMARY_EMAIL_KEY = "primaryEmail";

        @Override
        public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject userObject = json.getAsJsonObject();

            String id = asString(UID_KEY, userObject);
            String firstName = asString(FIRST_NAME_KEY, userObject);
            String lastName = asString(LAST_NAME_KEY, userObject);
            String primaryEmail = asString(PRIMARY_EMAIL_KEY, userObject);

            return new User(id, firstName, lastName, primaryEmail, "*");
        }
    }

    @Override
    public String toString() {return "com.mainstreetcode.teammates.model.User(id=" + this.id + ", firstName=" + this.firstName + ", lastName=" + this.lastName + ", primaryEmail=" + this.primaryEmail + ")";}

    public String getId() {return this.id;}

    public String getFirstName() {return this.firstName;}

    @SuppressWarnings("unused")
    public String getLastName() {return this.lastName;}

    public String getPrimaryEmail() {return this.primaryEmail;}

    public String getPassword() {return this.password;}
}
