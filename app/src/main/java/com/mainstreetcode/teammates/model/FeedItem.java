package com.mainstreetcode.teammates.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Items from a user's Feed
 */

public class FeedItem<T extends Model> {

    private final String type;
    private final String message;
    private final T model;

    FeedItem(String type, String message, T model) {
        this.type = type;
        this.message = message;
        this.model = model;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getImageUrl() {
        return model.getImageUrl();
    }

    public T getModel() {
        return model;
    }

    public static class GsonAdapter<T extends Model>
            implements JsonDeserializer<FeedItem<T>> {

        private static final String TYPE_KEY = "type";
        private static final String MODEL_TYPE_KEY = "modelName";
        private static final String MESSAGE_KEY = "message";
        private static final String MODEL_KEY = "model";

        public GsonAdapter() {
        }

        @Override
        public FeedItem<T> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject feedItemJson = json.getAsJsonObject();

            String type = ModelUtils.asString(TYPE_KEY, feedItemJson);
            String modelType = ModelUtils.asString(MODEL_TYPE_KEY, feedItemJson);
            String message = ModelUtils.asString(MESSAGE_KEY, feedItemJson);

            Class typeClass;

            switch (type != null ? type : "") {
                case "join-request":
                    typeClass = JoinRequest.class;
                    break;
                case "event":
                    typeClass = Event.class;
                    break;
                default:
                    typeClass = Object.class;
            }

            T model = context.deserialize(feedItemJson.get(MODEL_KEY), typeClass);

            return new FeedItem<>(modelType, message, model);
        }
    }
}
