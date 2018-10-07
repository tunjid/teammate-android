package com.mainstreetcode.teammate.notifications;


import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.repository.ModelRepository;
import com.mainstreetcode.teammate.repository.TournamentRepository;


public class CompetitorNotifier extends Notifier<Tournament> {

    private static CompetitorNotifier INSTANCE;

    private CompetitorNotifier() {

    }

    public static CompetitorNotifier getInstance() {
        if (INSTANCE == null) INSTANCE = new CompetitorNotifier();
        return INSTANCE;
    }

    @Override
    String getNotifyId() {return FeedItem.COMPETITOR;}

    @Override
    protected ModelRepository<Tournament> getRepository() {return TournamentRepository.getInstance();}

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected NotificationChannel[] getNotificationChannels() {
        return new NotificationChannel[]{buildNotificationChannel(FeedItem.COMPETITOR, R.string.competitor, R.string.competitors_notifier_description, NotificationManager.IMPORTANCE_DEFAULT)};
    }
}
