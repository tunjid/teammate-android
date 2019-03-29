package com.mainstreetcode.teammate.notifications;


import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.repository.ModelRepo;
import com.mainstreetcode.teammate.repository.RepoProvider;


public class JoinRequestNotifier extends Notifier<JoinRequest> {

    private static JoinRequestNotifier INSTANCE;

    private JoinRequestNotifier() {

    }

    public static JoinRequestNotifier getInstance() {
        if (INSTANCE == null) INSTANCE = new JoinRequestNotifier();
        return INSTANCE;
    }

    @Override
    String getNotifyId() {return FeedItem.JOIN_REQUEST;}

    @Override
    protected ModelRepo<JoinRequest> getRepository() { return RepoProvider.forModel(JoinRequest.class); }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected NotificationChannel[] getNotificationChannels() {
        return new NotificationChannel[]{buildNotificationChannel(FeedItem.JOIN_REQUEST, R.string.join_requests, R.string.join_requests_notifier_description, NotificationManager.IMPORTANCE_DEFAULT)};
    }
}
