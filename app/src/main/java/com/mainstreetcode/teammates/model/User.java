package com.mainstreetcode.teammates.model;

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

    String uid;
    String firstName;
    String lastName;
    String primaryEmail;
}
