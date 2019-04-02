package com.mainstreetcode.teammate.notifications;


import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.repository.ModelRepo;
import com.mainstreetcode.teammate.repository.RepoProvider;


public class CompetitorNotifier extends Notifier<Competitor> {

    CompetitorNotifier() {}

    @Override
    String getNotifyId() { return FeedItem.COMPETITOR; }

    @Override
    protected ModelRepo<Competitor> getRepository() { return RepoProvider.forModel(Competitor.class); }

    @Override
    @TargetApi(Build.VERSION_CODES.O)
    protected NotificationChannel[] getNotificationChannels() {
        return new NotificationChannel[]{buildNotificationChannel(FeedItem.COMPETITOR, R.string.competitor, R.string.competitors_notifier_description, NotificationManager.IMPORTANCE_DEFAULT)};
    }
}
