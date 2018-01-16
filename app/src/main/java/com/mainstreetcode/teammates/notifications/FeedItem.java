package com.mainstreetcode.teammates.notifications;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mainstreetcode.teammates.model.Chat;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Media;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.rest.TeammateService;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Notifications from a user's feed
 */

public class FeedItem<T extends Model<T>> implements Identifiable {

    static final String JOIN_REQUEST = "join-request";
    static final String EVENT = "event";
    static final String TEAM = "team";
    static final String ROLE = "role";
    static final String CHAT = "team-chat";
    static final String MEDIA = "team-media";

    private static final Gson gson = TeammateService.getGson();

    private final String action;
    private final String title;
    private final String body;
    private final String type;
    private final T model;
    final Class<T> itemClass;

    FeedItem(String action, String title, String body, String type, T model, Class<T> itemClass) {
        this.action = action;
        this.title = title;
        this.body = body;
        this.type = type;
        this.model = model;
        this.itemClass = itemClass;
    }

    @Nullable
    static <T extends Model<T>> FeedItem<T> fromNotification(RemoteMessage message) {
        Map<String, String> data = message.getData();
        if (data == null || data.isEmpty()) return null;

        FeedItem<T> result = null;

        try {result = gson.<FeedItem<T>>fromJson(gson.toJson(data), FeedItem.class);}
        catch (Exception e) {e.printStackTrace();}

        return result;
    }

    @Override
    public String getId() {
        return model.getId();
    }

    public String getType() {
        return type;
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

    boolean isDeleteAction() {return !TextUtils.isEmpty(action) && "DELETE".equals(action);}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FeedItem)) return false;

        FeedItem<?> feedItem = (FeedItem<?>) o;

        return model.equals(feedItem.model);
    }

    @Override
    public int hashCode() {
        return model.hashCode();
    }

    public static class GsonAdapter<T extends Model<T>>
            implements JsonDeserializer<FeedItem<T>> {

        private static final String ACTION_KEY = "action";
        private static final String TYPE_KEY = "type";
        private static final String TITLE_KEY = "title";
        private static final String BODY_KEY = "body";
        private static final String MODEL_KEY = "model";
        private static final String MODEL_ID_KEY = "_id";

        public GsonAdapter() {
        }

        @Override
        @SuppressWarnings("unchecked")
        public FeedItem<T> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject feedItemJson = json.getAsJsonObject();

            String action = ModelUtils.asString(ACTION_KEY, feedItemJson);
            String type = ModelUtils.asString(TYPE_KEY, feedItemJson);
            String title = ModelUtils.asString(TITLE_KEY, feedItemJson);
            String body = ModelUtils.asString(BODY_KEY, feedItemJson);

            Class typeClass;

            switch (type != null ? type : "") {
                case JOIN_REQUEST:
                    typeClass = JoinRequest.class;
                    break;
                case EVENT:
                    typeClass = Event.class;
                    break;
                case TEAM:
                    typeClass = Team.class;
                    break;
                case ROLE:
                    typeClass = Role.class;
                    break;
                case CHAT:
                    typeClass = Chat.class;
                    break;
                case MEDIA:
                    typeClass = Media.class;
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

            return new FeedItem<>(action, title, body, type, model, typeClass);
        }
    }
}
