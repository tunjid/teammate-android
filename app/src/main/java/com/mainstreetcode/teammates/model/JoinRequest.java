package com.mainstreetcode.teammates.model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.Map;

import lombok.Getter;
import lombok.experimental.Builder;

/**
 * Join request for a {@link Team}
 * <p>
 * Created by Shemanigans on 6/6/17.
 */

@Getter
@Builder
public class JoinRequest {

    public static final String DB_NAME = "joinRequests";
    public static final String TEAM_APPROVAL_KEY = "isTeamApproved";
    public static final String USER_APPROVAL_KEY = "isMemberApproved";
    public static final String TEAM_KEY = "teamId";
    public static final String USER_KEY = "memberId";
    public static final String ROLE_KEY = "roleId";

    boolean isTeamApproved;
    boolean isMemberApproved;
    String teamId;
    String memberId;
    String roleId;

    public static JoinRequest fromSnapshot(DataSnapshot snapshot) {
        Map<String, Object> data = snapshot.getValue(new GenericTypeIndicator<Map<String, Object>>() {
        });
        return builder().isTeamApproved((boolean) data.get(TEAM_APPROVAL_KEY))
                .isMemberApproved((boolean) data.get(USER_APPROVAL_KEY))
                .teamId((String) data.get(TEAM_KEY))
                .memberId((String) data.get(USER_KEY))
                .roleId((String) data.get(ROLE_KEY))
                .build();

    }
}
