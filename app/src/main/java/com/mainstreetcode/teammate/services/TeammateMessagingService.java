package com.mainstreetcode.teammate.services;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.notifications.FeedItem;
import com.mainstreetcode.teammate.notifications.Notifier;
import com.mainstreetcode.teammate.repository.ModelRepository;


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
            ModelRepository<T> repository = repositoryFactory.forClass(item.getItemClass());
            if (repository != null) repository.queueForLocalDeletion(item.getModel());
        }
        else {
            Notifier<T> notifier = factory.forClass(item.getItemClass());
            if (notifier != null) notifier.notify(item);
        }
    }

}
