package com.mainstreetcode.teammate.notifications;


import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.repository.ModelRepo;
import com.mainstreetcode.teammate.repository.RepoProvider;


public class GameNotifier extends Notifier<Game> {

    GameNotifier() {}

    @Override
    String getNotifyId() { return FeedItem.GAME; }

    @Override
    protected ModelRepo<Game> getRepository() { return RepoProvider.forModel(Game.class); }

    @Override
    @TargetApi(Build.VERSION_CODES.O)
    protected NotificationChannel[] getNotificationChannels() {
        return new NotificationChannel[]{buildNotificationChannel(FeedItem.GAME, R.string.games, R.string.games_notifier_description, NotificationManager.IMPORTANCE_DEFAULT)};
    }
}
