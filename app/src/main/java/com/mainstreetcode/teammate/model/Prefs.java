package com.mainstreetcode.teammate.model;


import android.annotation.SuppressLint;
import android.os.Parcel;
import androidx.annotation.NonNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

@SuppressLint("ParcelCreator")
public class Prefs implements Model<Prefs> {

    public static Prefs empty() {return new Prefs();}

    private boolean onBoarded;

    private Prefs() {}

    public boolean isOnBoarded() { return onBoarded; }

    public void setOnBoarded(boolean onBoarded) { this.onBoarded = onBoarded; }

    @Override
    public void update(Prefs updated) {
        this.onBoarded = updated.onBoarded;
    }

    @Override
    public int compareTo(@NonNull Prefs o) {
        return 0;
    }

    @Override
    public boolean isEmpty() { return false; }

    @Override
    public String getId() {
        return "1";
    }

    @Override
    public String getImageUrl() {
        return "";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) { }

    public static class GsonAdapter
            implements
            JsonSerializer<Prefs> {

        private static final String ON_BOARDED = "onBoarded";

        @Override
        public JsonElement serialize(Prefs src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject serialized = new JsonObject();

            serialized.addProperty(ON_BOARDED, src.onBoarded);

            return serialized;
        }
    }
}
