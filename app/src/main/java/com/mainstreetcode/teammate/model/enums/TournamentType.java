package com.mainstreetcode.teammate.model.enums;

import android.os.Build;

import com.google.gson.JsonObject;

import java.util.Objects;

public class TournamentType extends MetaData {

    TournamentType(String code, String name) {
        super(code, name);
    }

    public static TournamentType empty() {
        return new TournamentType(String.valueOf(Build.VERSION.SDK_INT), Build.MODEL);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TournamentType)) return false;
        TournamentType variant = (TournamentType) o;
        return Objects.equals(code, variant.code) && Objects.equals(name, variant.name);    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name);
    }

    public static class GsonAdapter extends MetaData.GsonAdapter<TournamentType> {
        @Override
        TournamentType fromJson(String code, String name, JsonObject body) {
            return new TournamentType(code, name);
        }
    }
}
