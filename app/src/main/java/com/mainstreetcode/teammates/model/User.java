package com.mainstreetcode.teammates.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import lombok.Getter;
import lombok.Setter;

/**
 * Users that may be part of a {@link Team}
 * <p>
 * Created by Shemanigans on 6/4/17.
 */

@Entity(tableName = "users")
@Getter
@Setter
public class User {

    public static final String DB_NAME = "users";
    public static final String UID_KEY = "uid";
    public static final String FIRST_NAME_KEY = "firstName";
    public static final String LAST_NAME_KEY = "lastName";
    public static final String PRIMARY_EMAIL_KEY = "primaryEmail";

    @PrimaryKey
    public String id;
    public String firstName;
    public String lastName;
    public String primaryEmail;
    public String password;

    public User(String id, String firstName, String lastName, String primaryEmail, String password) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.primaryEmail = primaryEmail;
        this.password = password;
    }

    public static UserBuilder builder() {return new UserBuilder();}

    public static class JsonDeserializer implements com.google.gson.JsonDeserializer<User> {
        @Override
        public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject userObject = json.getAsJsonObject();

            return builder().firstName(userObject.get(FIRST_NAME_KEY).getAsString())
                    .lastName(userObject.get(LAST_NAME_KEY).getAsString())
                    .primaryEmail(userObject.get(PRIMARY_EMAIL_KEY).getAsString())
                    .build();
        }
    }

    public static class UserBuilder {
        private String id;
        private String firstName;
        private String lastName;
        private String primaryEmail;
        private String password;

        UserBuilder() {}

        public User.UserBuilder id(String id) {
            this.id = id;
            return this;
        }

        public User.UserBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public User.UserBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public User.UserBuilder primaryEmail(String primaryEmail) {
            this.primaryEmail = primaryEmail;
            return this;
        }

        public User.UserBuilder password(String password) {
            this.password = password;
            return this;
        }

        public User build() {
            return new User(id, firstName, lastName, primaryEmail, password);
        }

        public String toString() {return "com.mainstreetcode.teammates.model.User.UserBuilder(id=" + this.id + ", firstName=" + this.firstName + ", lastName=" + this.lastName + ", primaryEmail=" + this.primaryEmail + ", password=" + this.password + ")";}
    }
}
