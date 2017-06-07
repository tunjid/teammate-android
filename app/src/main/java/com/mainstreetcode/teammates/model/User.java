package com.mainstreetcode.teammates.model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.Iterator;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Builder;

/**
 * Users that may be part of a {@link Team}
 * <p>
 * Created by Shemanigans on 6/4/17.
 */

@Getter
@Setter
@Builder
public class User {

    public static final String DB_NAME = "users";
    public static final String UID_KEY = "uid";
    public static final String FIRST_NAME_KEY = "firstName";
    public static final String LAST_NAME_KEY = "lastName";
    public static final String PRIMARY_EMAIL_KEY = "primaryEmail";

    String uid;
    String firstName;
    String lastName;
    String primaryEmail;

    public static User fromSnapshot(DataSnapshot snapshot) {
        UserBuilder builder = builder();

        if (!snapshot.hasChildren()) return builder.build();

        Iterator<DataSnapshot> snapshotIterator = snapshot.getChildren().iterator();

        if (snapshotIterator.hasNext()) {
            snapshot = snapshotIterator.next();

            Map<String, Object> data = snapshot.getValue(new GenericTypeIndicator<Map<String, Object>>() {
            });

            builder.uid((String) data.get(UID_KEY))
                    .firstName((String) data.get(FIRST_NAME_KEY))
                    .lastName((String) data.get(LAST_NAME_KEY))
                    .primaryEmail((String) data.get(PRIMARY_EMAIL_KEY));
        }
        return builder.build();
    }
}
