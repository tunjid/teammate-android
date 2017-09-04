package com.mainstreetcode.teammates.notifications;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mainstreetcode.teammates.model.Model;


public class TeammateMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        handleMessage(remoteMessage);
    }

    public <T extends Model<T> & Notifiable<T>> void handleMessage(RemoteMessage remoteMessage) {
        FeedItem<T> item = FeedItem.fromNotification(remoteMessage);
        if (item == null) return;

        item.getModel().getNotifier().notify(item);
    }
}
