package com.mainstreetcode.teammate.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammate.model.enums.Sport;

import java.lang.reflect.Type;

public class PublicEventRequest {

    private int distance;
    private Sport sport;
    private LatLng location;

    PublicEventRequest(int distance, Sport sport, LatLng location) {
        this.distance = distance;
        this.location = location;
        this.sport = sport;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private int distance;
        private Sport sport;
        private LatLng location;

        public Builder setDistance(int distance) {
            this.distance = distance;
            return this;
        }

        public Builder setSport(Sport sport) {
            this.sport = sport;
            return this;
        }

        public Builder setLocation(LatLng location) {
            this.location = location;
            return this;
        }

        public PublicEventRequest build() {
            return new PublicEventRequest(distance, sport, location);
        }
    }

    public static class GsonAdapter implements JsonSerializer<PublicEventRequest> {

        private static final String DISTANCE_KEY = "maxDistance";
        private static final String SPORT_KEY = "sport";

        private static final String LOCATION_KEY = "location";

        @Override
        public JsonElement serialize(PublicEventRequest src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject serialized = new JsonObject();

            serialized.addProperty(DISTANCE_KEY, src.distance);

            if (src.sport != null) {
                serialized.addProperty(SPORT_KEY, src.sport.getCode());
            }

            if (src.location != null) {
                JsonArray coordinates = new JsonArray();
                coordinates.add(src.location.longitude);
                coordinates.add(src.location.latitude);
                serialized.add(LOCATION_KEY, coordinates);
            }

            return serialized;
        }

    }
}
