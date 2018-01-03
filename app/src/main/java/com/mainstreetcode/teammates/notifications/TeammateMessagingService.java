package com.mainstreetcode.teammates.notifications;

import android.support.annotation.Nullable;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mainstreetcode.teammates.model.Chat;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Media;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;


public class TeammateMessagingService extends FirebaseMessagingService {

    private NotifierFactory factory = new NotifierFactory();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        handleMessage(remoteMessage);
    }

    public <T extends Model<T>> void handleMessage(RemoteMessage remoteMessage) {
        FeedItem<T> item = FeedItem.fromNotification(remoteMessage);
        if (item == null) return;

        Notifier<T> notifier = factory.forFeedItem(item.itemClass);
        if (notifier != null) notifier.notify(item);
    }

    static class NotifierFactory {

        @Nullable
        @SuppressWarnings("unchecked")
        <T extends Model<T>> Notifier<T> forFeedItem(Class<T> itemClass) {

            Notifier notifier = null;

            if (itemClass.equals(Team.class)) notifier = TeamNotifier.getInstance();
            if (itemClass.equals(Role.class)) notifier = RoleNotifier.getInstance();
            if (itemClass.equals(Chat.class)) notifier = ChatNotifier.getInstance();
            if (itemClass.equals(Media.class)) notifier = MediaNotifier.getInstance();
            if (itemClass.equals(Event.class)) notifier = EventNotifier.getInstance();
            if (itemClass.equals(JoinRequest.class)) notifier = JoinRequestNotifier.getInstance();

            return (Notifier<T>) notifier;
        }
    }
}
