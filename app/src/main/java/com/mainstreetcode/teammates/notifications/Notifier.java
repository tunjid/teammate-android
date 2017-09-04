package com.mainstreetcode.teammates.notifications;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.mainstreetcode.teammates.Application;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.activities.MainActivity;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.repository.ModelRespository;
import com.mainstreetcode.teammates.util.ErrorHandler;

import io.reactivex.functions.Predicate;

import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static android.app.PendingIntent.getActivity;
import static android.content.Context.NOTIFICATION_SERVICE;

public abstract class Notifier<T extends Model<T> & Notifiable<T>> {

    private static final int DEEP_LINK_REQ_CODE = 1;

    protected final Application app = Application.getInstance();

    final void notify(FeedItem<T> item) {
        getRepository().get(item.getModel()).lastElement()
                .filter(getNotificationFilter())
                .map(model -> item)
                .subscribe(this::handleNotification, ErrorHandler.EMPTY);
    }

    final NotificationCompat.Builder getNotificationBuilder(FeedItem<T> item) {
        return new NotificationCompat.Builder(app, item.getType());
    }

    PendingIntent getDeepLinkIntent(FeedItem<T> item) {
        Intent intent = new Intent(app, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(MainActivity.FEED_DEEP_LINK, item.getModel());

        return getActivity(app, DEEP_LINK_REQ_CODE, intent, FLAG_ONE_SHOT);
    }

    void sendNotification(Notification notification) {
        NotificationManager notifier = (NotificationManager) app.getSystemService(NOTIFICATION_SERVICE);
        notifier.notify(0, notification);
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

    protected abstract ModelRespository<T> getRepository();

    public Predicate<T> getNotificationFilter() {
        return t -> true;
    }

    public static <T extends Model<T> & Notifiable<T>> Notifier<T> defaultNotifier(ModelRespository<T> respository) {
        return new Notifier<T>() {
            @Override
            protected ModelRespository<T> getRepository() {
                return respository;
            }
        };
    }
}
