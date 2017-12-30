package com.mainstreetcode.teammates.model;


import android.annotation.SuppressLint;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.lang.reflect.Type;

@SuppressLint("ParcelCreator")
public class Device implements Model<Device> {

    private String id;
    private String fcmToken;
    private final String operatingSystem = "Android";

    public Device() {

    }
    public Device(String id) {
        this.id = id;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    @Override
    public void reset() {
        fcmToken = "";
    }

    @Override
    public void update(Device updated) {
        this.id = updated.id;
        this.fcmToken = updated.fcmToken;
    }

    @Override
    public int compareTo(@NonNull Device o) {
        return id.compareTo(o.id);
    }

    @Override
    public boolean isEmpty() {
        return TextUtils.isEmpty(id);
    }

    @Override
    public String getId() {
        return id;
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
    public void writeToParcel(Parcel dest, int flags) {

    }

    public static class GsonAdapter
            implements
            JsonSerializer<Device>,
            JsonDeserializer<Device> {

        private static final String ID_KEY = "_id";
        private static final String FCM_TOKEN_KEY = "fcmToken";
        private static final String OPERATING_SYSTEM_KEY = "operatingSystem";

        @Override
        public JsonElement serialize(Device src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject serialized = new JsonObject();

            serialized.addProperty(FCM_TOKEN_KEY, src.fcmToken);
            serialized.addProperty(OPERATING_SYSTEM_KEY, src.operatingSystem);

            return serialized;
        }

        @Override
        public Device deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonObject deviceJson = json.getAsJsonObject();

            String id = ModelUtils.asString(ID_KEY, deviceJson);
            String fcmToken = ModelUtils.asString(FCM_TOKEN_KEY, deviceJson);

            Device device = new Device(fcmToken);
            device.id = id;

            return device;
        }
    }
}
