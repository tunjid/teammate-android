package com.mainstreetcode.teammate.services;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mainstreetcode.teammate.model.Device;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.notifications.FeedItem;
import com.mainstreetcode.teammate.notifications.Notifier;
import com.mainstreetcode.teammate.repository.DeviceRepo;
import com.mainstreetcode.teammate.repository.RepoProvider;
import com.mainstreetcode.teammate.repository.UserRepo;
import com.mainstreetcode.teammate.util.ErrorHandler;


public class TeammateMessagingService extends FirebaseMessagingService {

    private Notifier.NotifierFactory factory = new Notifier.NotifierFactory();

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(result -> updateFcmToken(result.getToken()));
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) { handleMessage(remoteMessage); }

    @Override
    public void onNewToken(String token) { updateFcmToken(token); }

    public <T extends Model<T>> void handleMessage(RemoteMessage remoteMessage) {
        FeedItem<T> item = FeedItem.fromNotification(remoteMessage);
        if (item == null) return;

        if (item.isDeleteAction()) {
            RepoProvider.forModel(item.getItemClass()).queueForLocalDeletion(item.getModel());
        }
        else {
            Notifier<T> notifier = factory.forClass(item.getItemClass());
            if (notifier != null) notifier.notify(item);
        }
    }

    @SuppressLint("CheckResult")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void updateFcmToken(String token) {
        if (TextUtils.isEmpty(token) || RepoProvider.forRepo(UserRepo.class).getCurrentUser().isEmpty())
            return;

        RepoProvider.forRepo(DeviceRepo.class).createOrUpdate(Device.withFcmToken(token)).subscribe(__ -> {}, ErrorHandler.EMPTY);
    }

}
