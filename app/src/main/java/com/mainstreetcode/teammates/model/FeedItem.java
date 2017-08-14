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

public class FeedItem<T extends Model<T>> {

    private final String title;
    private final String body;
    private final T model;

    FeedItem(String title, String body, T model) {
        this.title = title;
        this.body = body;
        this.model = model;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getImageUrl() {
        return model.getImageUrl();
    }

    public T getModel() {
        return model;
    }

    public static class GsonAdapter<T extends Model<T>>
            implements JsonDeserializer<FeedItem<T>> {

        private static final String TYPE_KEY = "type";
        private static final String TITLE_KEY = "title";
        private static final String BODY_KEY = "body";
        private static final String MODEL_KEY = "model";
        private static final String MODEL_ID_KEY = "_id";

        public GsonAdapter() {
        }

        @Override
        public FeedItem<T> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject feedItemJson = json.getAsJsonObject();

            String type = ModelUtils.asString(TYPE_KEY, feedItemJson);
            String title = ModelUtils.asString(TITLE_KEY, feedItemJson);
            String body = ModelUtils.asString(BODY_KEY, feedItemJson);

            Class typeClass;

            switch (type != null ? type : "") {
                case "join-request":
                    typeClass = JoinRequest.class;
                    break;
                case "event":
                    typeClass = Event.class;
                    break;
                case "team":
                    typeClass = Team.class;
                    break;
                case "team-chat":
                    typeClass = TeamChat.class;
                    break;
                default:
                    typeClass = Object.class;
            }

            JsonElement modelElement = feedItemJson.get(MODEL_KEY);

            if (modelElement.isJsonPrimitive()) {
                String modelId = modelElement.getAsString();
                JsonObject modelBody = new JsonObject();

                modelBody.addProperty(MODEL_ID_KEY, modelId);
                modelElement = modelBody;
            }

            T model = context.deserialize(modelElement, typeClass);

            return new FeedItem<>(title, body, model);
        }
    }
}
