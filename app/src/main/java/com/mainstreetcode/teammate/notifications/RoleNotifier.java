package com.mainstreetcode.teammate.notifications;


import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.repository.ModelRepository;
import com.mainstreetcode.teammate.repository.RoleRepository;


public class RoleNotifier extends Notifier<Role> {

    private static RoleNotifier INSTANCE;

    private RoleNotifier() {

    }

    public static RoleNotifier getInstance() {
        if (INSTANCE == null) INSTANCE = new RoleNotifier();
        return INSTANCE;
    }

    @Override
    String getNotifyId() {return FeedItem.ROLE;}

    @Override
    protected ModelRepository<Role> getRepository() {return RoleRepository.getInstance();}

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected NotificationChannel[] getNotificationChannels() {
        return new NotificationChannel[]{buildNotificationChannel(FeedItem.ROLE, R.string.roles, R.string.role_notifier_description, NotificationManager.IMPORTANCE_DEFAULT)};
    }
}
