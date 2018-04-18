package com.mainstreetcode.teammate.model.enums;

import com.google.gson.JsonObject;

public class Visibility extends MetaData {

    private static final String PUBLIC = "public";
    private static final String PRIVATE = "private";

    Visibility(String code, String name) {
        super(code, name);
    }

    public static Visibility empty() {
        return new Visibility(PRIVATE, "Private");
    }

    public boolean isPublic() {return PUBLIC.equals(getCode());}

    public static class GsonAdapter extends MetaData.GsonAdapter<Visibility> {
        @Override
        Visibility with(String code, String name, JsonObject body) {
            return new Visibility(code, name);
        }
    }
}
