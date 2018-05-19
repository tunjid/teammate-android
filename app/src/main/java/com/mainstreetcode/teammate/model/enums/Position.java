package com.mainstreetcode.teammate.model.enums;

import com.google.gson.JsonObject;

public class Position extends MetaData {

    Position(String code, String name) {
        super(code, name);
    }

    public static Position empty() {
        return new Position("", "");
    }

    public static class GsonAdapter extends MetaData.GsonAdapter<Position> {
        @Override
        Position fromJson(String code, String name, JsonObject body) {
            return new Position(code, name);
        }
    }
}
