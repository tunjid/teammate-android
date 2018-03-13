package com.mainstreetcode.teammates.services;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.notifications.FeedItem;
import com.mainstreetcode.teammates.notifications.Notifier;
import com.mainstreetcode.teammates.repository.ModelRepository;


public class TeammateMessagingService extends FirebaseMessagingService {

    private Notifier.NotifierFactory factory = new Notifier.NotifierFactory();
    private ModelRepository.RepositoryFactory repositoryFactory = new ModelRepository.RepositoryFactory();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        handleMessage(remoteMessage);
    }

    public <T extends Model<T>> void handleMessage(RemoteMessage remoteMessage) {
        FeedItem<T> item = FeedItem.fromNotification(remoteMessage);
        if (item == null) return;

        if (item.isDeleteAction()) {
            ModelRepository<T> repository = repositoryFactory.forFeedItem(item.getItemClass());
            if (repository != null) repository.delete(item.getModel());
        }
        else {
            Notifier<T> notifier = factory.forFeedItem(item.getItemClass());
            if (notifier != null) notifier.notify(item);
        }
    }

}
