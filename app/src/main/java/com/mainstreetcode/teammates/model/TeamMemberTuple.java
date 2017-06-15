package com.mainstreetcode.teammates.model;

import android.arch.persistence.room.Entity;

import lombok.Getter;

/**
 * A tuple for {@link User}-{@link Team} relationships
 * <p>
 * Created by Shemanigans on 6/13/17.
 */

@Entity(primaryKeys = {"memberId", "teamId"})
@Getter
public class TeamMemberTuple {
    String memberId;
    String teamId;
}
