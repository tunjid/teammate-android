package com.mainstreetcode.teammate.model;

import android.support.annotation.Nullable;

public class TeamSearchRequest {

    private String name;
    private String sport;

    public static TeamSearchRequest from(@Nullable Tournament tournament) {
        TeamSearchRequest request = new TeamSearchRequest();
        if (tournament != null) request.sport = tournament.getSport().getCode();
        return request;
    }

    private TeamSearchRequest() {}

    public TeamSearchRequest query(String query) {
        this.name = query;
        return this;
    }

    public String getName() { return name; }

    @Nullable
    public String getSport() { return sport; }
}
