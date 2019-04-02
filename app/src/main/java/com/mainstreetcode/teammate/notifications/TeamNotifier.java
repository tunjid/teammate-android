package com.mainstreetcode.teammate.notifications;


import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.repository.ModelRepo;
import com.mainstreetcode.teammate.repository.RepoProvider;


public class TeamNotifier extends Notifier<Team> {

    TeamNotifier() {}

    @Override
    String getNotifyId() { return FeedItem.TEAM; }

    @Override
    protected ModelRepo<Team> getRepository() { return RepoProvider.forModel(Team.class); }

    @Override
    @TargetApi(Build.VERSION_CODES.O)
    protected NotificationChannel[] getNotificationChannels() {
        return new NotificationChannel[]{buildNotificationChannel(FeedItem.TEAM, R.string.teams, R.string.team_notifier_description, NotificationManager.IMPORTANCE_DEFAULT)};
    }
}
