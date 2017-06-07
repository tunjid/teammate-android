package com.mainstreetcode.teammates.model;

import lombok.Getter;

/**
 * Join request for a {@link Team}
 * <p>
 * Created by Shemanigans on 6/6/17.
 */

@Getter

public class JoinRequest {

    boolean isTeamApproved;
    boolean isMemberApproved;
    String teamId;
    String memberId;
    String roleId;
}
