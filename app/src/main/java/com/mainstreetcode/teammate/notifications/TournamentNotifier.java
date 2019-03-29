package com.mainstreetcode.teammate.notifications;


import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.repository.ModelRepo;
import com.mainstreetcode.teammate.repository.RepoProvider;


public class TournamentNotifier extends Notifier<Tournament> {

    private static TournamentNotifier INSTANCE;

    private TournamentNotifier() {

    }

    public static TournamentNotifier getInstance() {
        if (INSTANCE == null) INSTANCE = new TournamentNotifier();
        return INSTANCE;
    }

    @Override
    String getNotifyId() {return FeedItem.TOURNAMENT;}

    @Override
    protected ModelRepo<Tournament> getRepository() { return RepoProvider.forModel(Tournament.class); }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected NotificationChannel[] getNotificationChannels() {
        return new NotificationChannel[]{buildNotificationChannel(FeedItem.TOURNAMENT, R.string.tournaments, R.string.tournaments_notifier_description, NotificationManager.IMPORTANCE_DEFAULT)};
    }
}
