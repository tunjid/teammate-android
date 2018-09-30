package com.mainstreetcode.teammate.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Roles on a team
 */

public class HeadToHeadResult {

    private final List<Aggregate> aggregates = new ArrayList<>();

    private static class Aggregate {
        int count;
        String id;

        Aggregate(int count, String id) {
            this.count = count;
            this.id = id;
        }
    }

    public static class GsonAdapter
            implements
            JsonDeserializer<HeadToHeadResult> {

        private static final String ID = "_id";
        private static final String COUNT = "count";
        private static final String WINNER = "winner";

        @Override
        public HeadToHeadResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonArray jsonArray = json.getAsJsonArray();
            HeadToHeadResult result = new HeadToHeadResult();

            for (JsonElement element : jsonArray) {
                JsonObject object = element.getAsJsonObject();
                int count = (int) ModelUtils.asFloat(COUNT, object);
                String id = ModelUtils.asString(WINNER, object.get(ID).getAsJsonObject());

                result.aggregates.add(new Aggregate(count, id));
            }

            return result;
        }
    }
}
