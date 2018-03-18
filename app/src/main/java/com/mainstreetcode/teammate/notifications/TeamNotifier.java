package com.mainstreetcode.teammate.notifications;


import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.repository.ModelRepository;
import com.mainstreetcode.teammate.repository.TeamRepository;


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
    protected ModelRepository<Team> getRepository() {return TeamRepository.getInstance();}

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected NotificationChannel[] getNotificationChannels() {
        return new NotificationChannel[]{buildNotificationChannel(FeedItem.TEAM, R.string.teams, R.string.team_notifier_description, NotificationManager.IMPORTANCE_DEFAULT)};
    }
}
