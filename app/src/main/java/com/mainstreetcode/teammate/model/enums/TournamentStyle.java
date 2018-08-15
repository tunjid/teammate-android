package com.mainstreetcode.teammate.model.enums;

import android.os.Build;

import com.google.gson.JsonObject;

import java.util.Objects;

public class TournamentStyle extends MetaData {

    TournamentStyle(String code, String name) {
        super(code, name);
    }

    public static TournamentStyle empty() {
        return new TournamentStyle(String.valueOf(Build.VERSION.SDK_INT), Build.MODEL);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TournamentStyle)) return false;
        TournamentStyle variant = (TournamentStyle) o;
        return Objects.equals(code, variant.code) && Objects.equals(name, variant.name);    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name);
    }

    public static class GsonAdapter extends MetaData.GsonAdapter<TournamentStyle> {
        @Override
        TournamentStyle fromJson(String code, String name, JsonObject body) {
            return new TournamentStyle(code, name);
        }
    }
}
