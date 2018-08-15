package com.mainstreetcode.teammate.model.enums;

import android.os.Build;

import com.google.gson.JsonObject;

import java.util.Objects;

public class GameStat extends MetaData {

    GameStat(String code, String name) {
        super(code, name);
    }

    public static GameStat empty() {
        return new GameStat(String.valueOf(Build.VERSION.SDK_INT), Build.MODEL);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameStat)) return false;
        GameStat variant = (GameStat) o;
        return Objects.equals(code, variant.code) && Objects.equals(name, variant.name);    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name);
    }

    public static class GsonAdapter extends MetaData.GsonAdapter<GameStat> {
        @Override
        GameStat fromJson(String code, String name, JsonObject body) {
            return new GameStat(code, name);
        }
    }
}
