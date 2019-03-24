package com.mainstreetcode.teammate.notifications;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mainstreetcode.teammate.model.Chat;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.util.FunctionalDiff;
import com.mainstreetcode.teammate.model.Game;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Media;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.rest.TeammateService;
import com.mainstreetcode.teammate.util.Logger;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Notifications from a user's feed
 */

public class FeedItem<T extends Model<T>> implements
        Parcelable,
        Differentiable,
        Comparable<FeedItem> {

    static final String JOIN_REQUEST = "join-request";
    static final String EVENT = "event";
    static final String TEAM = "team";
    static final String ROLE = "role";
    static final String CHAT = "team-chat";
    static final String MEDIA = "team-media";
    static final String TOURNAMENT = "tournament";
    static final String COMPETITOR = "competitor";
    static final String GAME = "game";

    private static final Gson gson = TeammateService.getGson();

    private final String action;
    private final String title;
    private final String body;
    private final String type;
    private final T model;
    private final Class<T> itemClass;

    private FeedItem(String action, String title, String body, String type, T model, Class<T> itemClass) {
        this.action = action;
        this.title = title;
        this.body = body;
        this.type = type;
        this.model = model;
        this.itemClass = itemClass;
    }

    @SuppressWarnings("unchecked")
    private FeedItem(Parcel in) {
        action = in.readString();
        title = in.readString();
        body = in.readString();
        type = in.readString();
        itemClass = forType(type);
        model = (T) in.readValue(itemClass.getClassLoader());
    }

    @Nullable
    public static <T extends Model<T>> FeedItem<T> fromNotification(RemoteMessage message) {
        Map<String, String> data = message.getData();
        if (data == null || data.isEmpty()) return null;

        FeedItem<T> result = null;

        try {result = gson.<FeedItem<T>>fromJson(gson.toJson(data), FeedItem.class);}
        catch (Exception e) {Logger.log("FeedItem", "Failed to parse feed item", e);}

        return result;
    }

    @Override
    public String getId() {return model.getId();}

    public String getType() {return type;}

    public String getTitle() {return title;}

    public String getBody() {return body;}

    public String getImageUrl() {return model.getImageUrl();}

    public T getModel() {return model;}

    public Class<T> getItemClass() {return itemClass;}

    public boolean isDeleteAction() {return !TextUtils.isEmpty(action) && "DELETE".equals(action);}

    @Override
    public int compareTo(@NonNull FeedItem o) {
        return FunctionalDiff.COMPARATOR.compare(model, o.getModel());
    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(action);
        dest.writeString(title);
        dest.writeString(body);
        dest.writeString(type);
        dest.writeValue(model);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<FeedItem> CREATOR = new Parcelable.Creator<FeedItem>() {
        @Override
        public FeedItem createFromParcel(Parcel in) {
            return new FeedItem(in);
        }

        @Override
        public FeedItem[] newArray(int size) {
            return new FeedItem[size];
        }
    };

    private static Class forType(String type) {
        switch (type != null ? type : "") {
            case JOIN_REQUEST:
                return JoinRequest.class;
            case TOURNAMENT:
                return Tournament.class;
            case COMPETITOR:
                return Competitor.class;
            case EVENT:
                return Event.class;
            case TEAM:
                return Team.class;
            case ROLE:
                return Role.class;
            case CHAT:
                return Chat.class;
            case GAME:
                return Game.class;
            case MEDIA:
                return Media.class;
            default:
                return Object.class;
        }
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
            Class itemClass = forType(type);

            JsonElement modelElement = feedItemJson.get(MODEL_KEY);

            if (modelElement.isJsonPrimitive()) {
                String modelId = modelElement.getAsString();
                JsonObject modelBody = new JsonObject();

                modelBody.addProperty(MODEL_ID_KEY, modelId);
                modelElement = modelBody;
            }

            T model = context.deserialize(modelElement, itemClass);
            return new FeedItem<>(action, title, body, type, model, itemClass);
        }
    }
}
