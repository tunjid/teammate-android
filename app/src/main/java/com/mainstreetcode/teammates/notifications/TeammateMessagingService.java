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
import com.mainstreetcode.teammates.repository.ChatRepository;
import com.mainstreetcode.teammates.repository.EventRepository;
import com.mainstreetcode.teammates.repository.JoinRequestRepository;
import com.mainstreetcode.teammates.repository.MediaRepository;
import com.mainstreetcode.teammates.repository.ModelRepository;
import com.mainstreetcode.teammates.repository.RoleRepository;
import com.mainstreetcode.teammates.repository.TeamRepository;


public class TeammateMessagingService extends FirebaseMessagingService {

    private NotifierFactory factory = new NotifierFactory();
    private RepositoryFactory repositoryFactory = new RepositoryFactory();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        handleMessage(remoteMessage);
    }

    public <T extends Model<T>> void handleMessage(RemoteMessage remoteMessage) {
        FeedItem<T> item = FeedItem.fromNotification(remoteMessage);
        if (item == null) return;

        if (item.isDeleteAction()) {
            ModelRepository<T> repository = repositoryFactory.forFeedItem(item.itemClass);
            if (repository != null) repository.delete(item.getModel());
        }
        else {
            Notifier<T> notifier = factory.forFeedItem(item.itemClass);
            if (notifier != null) notifier.notify(item);
        }
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

    static class RepositoryFactory {

        @Nullable
        @SuppressWarnings("unchecked")
        <T extends Model<T>> ModelRepository<T> forFeedItem(Class<T> itemClass) {

            ModelRepository repository = null;

            if (itemClass.equals(Team.class)) repository = TeamRepository.getInstance();
            if (itemClass.equals(Role.class)) repository = RoleRepository.getInstance();
            if (itemClass.equals(Chat.class)) repository = ChatRepository.getInstance();
            if (itemClass.equals(Media.class)) repository = MediaRepository.getInstance();
            if (itemClass.equals(Event.class)) repository = EventRepository.getInstance();
            if (itemClass.equals(JoinRequest.class))
                repository = JoinRequestRepository.getInstance();

            return (ModelRepository<T>) repository;
        }
    }
}
