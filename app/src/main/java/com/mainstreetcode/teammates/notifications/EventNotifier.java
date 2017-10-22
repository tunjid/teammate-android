package com.mainstreetcode.teammates.notifications;


import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.repository.EventRepository;
import com.mainstreetcode.teammates.repository.ModelRepository;


public class EventNotifier extends Notifier<Event> {

    private static EventNotifier INSTANCE;

    private EventNotifier() {

    }

    public static EventNotifier getInstance() {
        if (INSTANCE == null) INSTANCE = new EventNotifier();
        return INSTANCE;
    }

    @Override
    protected ModelRepository<Event> getRepository() {
        return EventRepository.getInstance();
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected NotificationChannel[] getNotificationChannels() {
        return new NotificationChannel[]{buildNotificationChannel(FeedItem.EVENT, R.string.events, R.string.events_notifier_description, NotificationManager.IMPORTANCE_DEFAULT)};
    }
}
