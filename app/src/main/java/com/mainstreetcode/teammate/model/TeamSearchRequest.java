package com.mainstreetcode.teammate.model;

import androidx.annotation.Nullable;

import android.text.TextUtils;

import java.util.Objects;

public class TeamSearchRequest {

    private String name;
    private String screenName;
    private String sport;

    public static TeamSearchRequest from(@Nullable String sportCode) {
        TeamSearchRequest request = new TeamSearchRequest();
        if (!TextUtils.isEmpty(sportCode)) request.sport = sportCode;
        return request;
    }

    private TeamSearchRequest() {}

    public TeamSearchRequest query(String query) {
        TeamSearchRequest request = new TeamSearchRequest();
        request.name = query.startsWith("@") ? "" : query;
        request.screenName = query.startsWith("@") ? query.replaceFirst("@", "") : "";
        request.sport = this.sport;
        return request;
    }

    public String getName() { return name; }

    public String getScreenName() {
        return screenName;
    }

    @Nullable
    public String getSport() { return sport; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TeamSearchRequest)) return false;
        TeamSearchRequest that = (TeamSearchRequest) o;
        return Objects.equals(name, that.name)
                && Objects.equals(screenName, that.screenName)
                && Objects.equals(sport, that.sport);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, screenName, sport);
    }
}
