package com.mainstreetcode.teammate.notifications;


import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.repository.ModelRepo;
import com.mainstreetcode.teammate.repository.RepoProvider;


public class EventNotifier extends Notifier<Event> {

    private static EventNotifier INSTANCE;

    private EventNotifier() {

    }

    public static EventNotifier getInstance() {
        if (INSTANCE == null) INSTANCE = new EventNotifier();
        return INSTANCE;
    }

    @Override
    String getNotifyId() {return FeedItem.EVENT;}

    @Override
    protected ModelRepo<Event> getRepository() { return RepoProvider.forModel(Event.class); }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected NotificationChannel[] getNotificationChannels() {
        return new NotificationChannel[]{buildNotificationChannel(FeedItem.EVENT, R.string.events, R.string.events_notifier_description, NotificationManager.IMPORTANCE_DEFAULT)};
    }
}
