package com.mainstreetcode.teammates.notifications;


import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.repository.ModelRespository;
import com.mainstreetcode.teammates.repository.TeamRepository;


public class TeamNotifier extends Notifier<Team> {

    private static TeamNotifier INSTANCE;

    private TeamNotifier() {

    }

    public static TeamNotifier getInstance() {
        if (INSTANCE == null) INSTANCE = new TeamNotifier();
        return INSTANCE;
    }

    @Override
    protected ModelRespository<Team> getRepository() {
        return TeamRepository.getInstance();
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected NotificationChannel[] getNotificationChannels() {
        return new NotificationChannel[]{buildNotificationChannel(FeedItem.TEAM, R.string.team, R.string.team_notifier_description, NotificationManager.IMPORTANCE_DEFAULT)};
    }
}
