package com.mainstreetcode.teammate.notifications;


import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.repository.ModelRepo;
import com.mainstreetcode.teammate.repository.RepoProvider;


public class RoleNotifier extends Notifier<Role> {

    RoleNotifier() {}

    @Override
    String getNotifyId() { return FeedItem.ROLE; }

    @Override
    protected ModelRepo<Role> getRepository() { return RepoProvider.forModel(Role.class); }

    @Override
    @TargetApi(Build.VERSION_CODES.O)
    protected NotificationChannel[] getNotificationChannels() {
        return new NotificationChannel[]{buildNotificationChannel(FeedItem.ROLE, R.string.roles, R.string.role_notifier_description, NotificationManager.IMPORTANCE_DEFAULT)};
    }
}
