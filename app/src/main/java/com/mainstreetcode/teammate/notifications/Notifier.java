package com.mainstreetcode.teammate.notifications;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.activities.MainActivity;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.repository.ModelRepo;
import com.mainstreetcode.teammate.util.ErrorHandler;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;
import io.reactivex.functions.Predicate;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;
import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static android.app.PendingIntent.getActivity;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.O;

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

    public void clearNotifications(@Nullable T model) {
        NotificationManager notifier = (NotificationManager) app.getSystemService(NOTIFICATION_SERVICE);
        if (notifier == null) return;

        if (model == null) notifier.cancel(getNotifyId().hashCode());
        else notifier.cancel(getNotificationTag(model), getNotifyId().hashCode());
    }

    @SuppressLint("CheckResult")
    public final void notify(FeedItem<T> item) {
        //noinspection ResultOfMethodCallIgnored
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

    PendingIntent getDeepLinkIntent(T model) {
        Intent intent = new Intent(app, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(MainActivity.FEED_DEEP_LINK, model);
        addNotificationId(intent);

        return getActivity(app, DEEP_LINK_REQ_CODE, intent, FLAG_ONE_SHOT);
    }

    void sendNotification(Notification notification, T model) {
        NotificationManager notifier = (NotificationManager) app.getSystemService(NOTIFICATION_SERVICE);
        if (notifier != null)
            notifier.notify(getNotificationTag(model), getNotifyId().hashCode(), notification);
    }

    void addNotificationId(Intent intent) {
        if (SDK_INT >= O) intent.putExtra(EXTRA_NOTIFICATION_ID, getNotifyId());
    }

    protected void handleNotification(FeedItem<T> item) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        sendNotification(getNotificationBuilder(item)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(item.getTitle())
                .setContentText(item.getBody())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(getDeepLinkIntent(item.getModel()))
                .build(), item.getModel());
    }

    @TargetApi(Build.VERSION_CODES.O)
    NotificationChannel buildNotificationChannel(String id, @StringRes int name, @StringRes int description, int importance) {
        NotificationChannel channel = new NotificationChannel(id, app.getString(name), importance);
        channel.setDescription(app.getString(description));

        return channel;
    }

    abstract String getNotifyId();

    String getNotificationTag(T model) {return model.getId();}

    protected abstract ModelRepo<T> getRepository();

    protected abstract NotificationChannel[] getNotificationChannels();

    public Predicate<T> getNotificationFilter() {
        return t -> true;
    }

}
