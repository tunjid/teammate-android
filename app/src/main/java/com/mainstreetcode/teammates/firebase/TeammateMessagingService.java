package com.mainstreetcode.teammates.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.activities.MainActivity;
import com.mainstreetcode.teammates.model.FeedItem;
import com.mainstreetcode.teammates.repository.CrudRespository;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.rest.TeammateService;
import com.mainstreetcode.teammates.util.ErrorHandler;

import java.util.Map;


public class TeammateMessagingService extends FirebaseMessagingService {

    private static final Gson gson = TeammateService.getGson();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        sendNotification(parseFeedItem(remoteMessage));
    }

    private <T extends Model<T>> void sendNotification(@Nullable final FeedItem<T> item) {
        if (item == null) return;

        T model = item.getModel();
        CrudRespository<T> respository = model.getRepository();

        respository.get(model).lastElement().subscribe(onSuccess -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(item.getTitle())
                    .setContentText(item.getBody())
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0, notificationBuilder.build());
        }, ErrorHandler.EMPTY);
    }

    @Nullable
    private <T extends Model<T>> FeedItem<T> parseFeedItem(RemoteMessage message) {
        Map<String, String> data = message.getData();
        if (data == null || data.isEmpty()) return null;

        FeedItem<T> result = null;

        try {result = gson.<FeedItem<T>>fromJson(gson.toJson(data), FeedItem.class);}
        catch (Exception e) {e.printStackTrace();}

        return result;
    }
}
