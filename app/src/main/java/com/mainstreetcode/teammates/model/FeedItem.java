package com.mainstreetcode.teammates.model;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mainstreetcode.teammates.Application;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.activities.MainActivity;
import com.mainstreetcode.teammates.repository.ModelRespository;
import com.mainstreetcode.teammates.rest.TeammateService;
import com.mainstreetcode.teammates.util.ErrorHandler;

import java.lang.reflect.Type;
import java.util.Map;

import static android.app.PendingIntent.*;
import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Notifications from a user's feed
 */

public class FeedItem<T extends Model<T>> {

    private static final int DEEP_LINK_REQ_CODE = 1;

    private static final String JOIN_REQUEST = "join-request";
    private static final String EVENT = "event";
    private static final String TEAM = "team";
    private static final String TEAM_CHAT = "team-chat";

    private static final Gson gson = TeammateService.getGson();

    private final Application app = Application.getInstance();

    private final String title;
    private final String body;
    private final String type;
    private final T model;

    FeedItem(String title, String body, String type, T model) {
        this.title = title;
        this.body = body;
        this.type = type;
        this.model = model;
    }

    @Nullable
    public static <T extends Model<T>> FeedItem<T> fromNotification(RemoteMessage message) {
        Map<String, String> data = message.getData();
        if (data == null || data.isEmpty()) return null;

        FeedItem<T> result = null;

        try {result = gson.<FeedItem<T>>fromJson(gson.toJson(data), FeedItem.class);}
        catch (Exception e) {e.printStackTrace();}

        return result;
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

    public void handleNotification() {
        ModelRespository<T> respository = model.getRepository();

        respository.get(model).lastElement()
                .filter(model.getRepository().getNotificationFilter())
                .subscribe(this::sendNotification, ErrorHandler.EMPTY);
    }

    private void sendNotification(T model) {
        Intent intent = new Intent(app, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(MainActivity.FEED_DEEP_LINK, model);

        PendingIntent pendingIntent = getActivity(app, DEEP_LINK_REQ_CODE, intent, FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(app, type)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notifier = (NotificationManager) app.getSystemService(NOTIFICATION_SERVICE);
        notifier.notify(0, notificationBuilder.build());
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
                case JOIN_REQUEST:
                    typeClass = JoinRequest.class;
                    break;
                case EVENT:
                    typeClass = Event.class;
                    break;
                case TEAM:
                    typeClass = Team.class;
                    break;
                case TEAM_CHAT:
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

            return new FeedItem<>(title, body, type, model);
        }
    }
}
