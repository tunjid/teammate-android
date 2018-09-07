package com.mainstreetcode.teammate.model.enums;

import android.os.Build;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import java.util.Objects;

public class AndroidVariant extends MetaData {

    AndroidVariant(String code, String name) {
        super(code, name);
    }

    public static AndroidVariant empty() {
        return new AndroidVariant(String.valueOf(Build.VERSION.SDK_INT), Build.MODEL);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AndroidVariant)) return false;
        AndroidVariant variant = (AndroidVariant) o;
        return Objects.equals(code, variant.code) && Objects.equals(name, variant.name);    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name);
    }

    public static class GsonAdapter extends MetaData.GsonAdapter<AndroidVariant> {
        @Override
        AndroidVariant fromJson(String code, String name, JsonObject body, JsonDeserializationContext context) {
            return new AndroidVariant(code, name);
        }
    }
}
