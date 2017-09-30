package com.mainstreetcode.teammates.notifications;


import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.repository.JoinRequestRepository;
import com.mainstreetcode.teammates.repository.ModelRespository;


public class JoinRequestNotifier extends Notifier<JoinRequest> {

    private static JoinRequestNotifier INSTANCE;

    private JoinRequestNotifier() {

    }

    public static JoinRequestNotifier getInstance() {
        if (INSTANCE == null) INSTANCE = new JoinRequestNotifier();
        return INSTANCE;
    }

    @Override
    protected ModelRespository<JoinRequest> getRepository() {
        return JoinRequestRepository.getInstance();
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected NotificationChannel[] getNotificationChannels() {
        return new NotificationChannel[]{buildNotificationChannel(FeedItem.JOIN_REQUEST, R.string.join_requests, R.string.join_requests_notifier_description, NotificationManager.IMPORTANCE_DEFAULT)};
    }
}
