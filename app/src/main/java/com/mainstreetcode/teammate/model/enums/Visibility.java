package com.mainstreetcode.teammate.model.enums;

import com.google.gson.JsonObject;

public class Visibility extends MetaData {
    Visibility(String code, String name) {
        super(code, name);
    }

    public static Visibility empty() {
        return new Visibility("private", "Private");
    }

    public static class GsonAdapter extends MetaData.GsonAdapter<Visibility> {
        @Override
        Visibility with(String code, String name, JsonObject body) {
            return new Visibility(code, name);
        }
    }
}
