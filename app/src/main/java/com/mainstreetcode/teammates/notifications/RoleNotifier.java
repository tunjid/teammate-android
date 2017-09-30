package com.mainstreetcode.teammates.notifications;


import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.repository.ModelRespository;
import com.mainstreetcode.teammates.repository.RoleRepository;


public class RoleNotifier extends Notifier<Role> {

    private static RoleNotifier INSTANCE;

    private RoleNotifier() {

    }

    public static RoleNotifier getInstance() {
        if (INSTANCE == null) INSTANCE = new RoleNotifier();
        return INSTANCE;
    }

    @Override
    protected ModelRespository<Role> getRepository() {
        return RoleRepository.getInstance();
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected NotificationChannel[] getNotificationChannels() {
        return new NotificationChannel[]{buildNotificationChannel(FeedItem.ROLE, R.string.roles, R.string.role_notifier_description, NotificationManager.IMPORTANCE_DEFAULT)};
    }
}
