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

    private static TeamNotifier INSTANCE;

    private TeamNotifier() {

    }

    public static TeamNotifier getInstance() {
        if (INSTANCE == null) INSTANCE = new TeamNotifier();
        return INSTANCE;
    }

    @Override
    String getNotifyId() {return FeedItem.TEAM;}

    @Override
    protected ModelRepo<Team> getRepository() { return RepoProvider.forModel(Team.class); }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected NotificationChannel[] getNotificationChannels() {
        return new NotificationChannel[]{buildNotificationChannel(FeedItem.TEAM, R.string.teams, R.string.team_notifier_description, NotificationManager.IMPORTANCE_DEFAULT)};
    }
}
