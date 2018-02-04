package com.mainstreetcode.teammates.notifications;


import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;

import com.mainstreetcode.teammates.App;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.activities.MainActivity;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.repository.ModelRepository;
import com.mainstreetcode.teammates.util.ErrorHandler;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.functions.Predicate;

import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static android.app.PendingIntent.getActivity;
import static android.content.Context.NOTIFICATION_SERVICE;

public abstract class Notifier<T extends Model<T>> {

    private static final int DEEP_LINK_REQ_CODE = 1;

    protected final App app;
    private final Map<String, NotificationChannel> channelMap;

    public Notifier() {
        app = App.getInstance();
        channelMap = new HashMap<>();

        NotificationManager manager = (NotificationManager) app.getSystemService(NOTIFICATION_SERVICE);
        if (manager == null) return;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel[] channels = getNotificationChannels();
            if (channels == null || channels.length < 1) return;

            for (NotificationChannel channel : channels) {
                manager.createNotificationChannel(channel);
                channelMap.put(channel.getId(), channel);
            }
        }
    }

    final void notify(FeedItem<T> item) {
        getRepository().get(item.getModel()).lastElement()
                .filter(getNotificationFilter())
                .map(model -> item)
                .subscribe(this::handleNotification, ErrorHandler.EMPTY);
    }

    final NotificationCompat.Builder getNotificationBuilder(FeedItem<T> item) {
        String type = item.getType();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(app, type);

        if (channelMap.containsKey(type)) builder.setChannelId(type);
        return builder;
    }

    PendingIntent getDeepLinkIntent(FeedItem<T> item) {
        Intent intent = new Intent(app, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(MainActivity.FEED_DEEP_LINK, item.getModel());

        return getActivity(app, DEEP_LINK_REQ_CODE, intent, FLAG_ONE_SHOT);
    }

    void sendNotification(Notification notification) {
        NotificationManager notifier = (NotificationManager) app.getSystemService(NOTIFICATION_SERVICE);
        if (notifier != null) notifier.notify(0, notification);
    }

    protected void handleNotification(FeedItem<T> item) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        sendNotification(getNotificationBuilder(item)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(item.getTitle())
                .setContentText(item.getBody())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(getDeepLinkIntent(item))
                .build());
    }

    @TargetApi(Build.VERSION_CODES.O)
    NotificationChannel buildNotificationChannel(String id, @StringRes int name, @StringRes int description, int importance) {
        NotificationChannel channel = new NotificationChannel(id, app.getString(name), importance);
        channel.setDescription(app.getString(description));

        return channel;
    }

    protected abstract ModelRepository<T> getRepository();

    protected abstract NotificationChannel[] getNotificationChannels();

    public Predicate<T> getNotificationFilter() {
        return t -> true;
    }
}
