package com.mainstreetcode.teammate.notifications;

import android.app.NotificationChannel;
import android.util.Pair;

import com.mainstreetcode.teammate.model.Chat;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Media;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.repository.ModelRepo;
import com.mainstreetcode.teammate.repository.RepoProvider;
import com.mainstreetcode.teammate.util.Logger;
import com.mainstreetcode.teammate.util.SingletonCache;

public class NotifierProvider {

    private static NotifierProvider ourInstance;

    private final SingletonCache<Model, Notifier> singletonCache;

    private NotifierProvider() {
        singletonCache = new SingletonCache<Model, Notifier>(itemClass -> {
            if (itemClass.equals(Team.class)) return TeamNotifier.class;
            if (itemClass.equals(Role.class)) return RoleNotifier.class;
            if (itemClass.equals(Chat.class)) return ChatNotifier.class;
            if (itemClass.equals(Game.class)) return GameNotifier.class;
            if (itemClass.equals(Media.class)) return MediaNotifier.class;
            if (itemClass.equals(Event.class)) return EventNotifier.class;
            if (itemClass.equals(Tournament.class)) return TournamentNotifier.class;
            if (itemClass.equals(Competitor.class)) return CompetitorNotifier.class;
            if (itemClass.equals(JoinRequest.class)) return JoinRequestNotifier.class;
            return falseNotifier.getClass();
        }, NotifierProvider::get,
                new Pair<>(falseNotifier.getClass(), falseNotifier),
                new Pair<>(TeamNotifier.class, new TeamNotifier()),
                new Pair<>(RoleNotifier.class, new RoleNotifier()),
                new Pair<>(ChatNotifier.class, new ChatNotifier()),
                new Pair<>(GameNotifier.class, new GameNotifier()),
                new Pair<>(MediaNotifier.class, new MediaNotifier()),
                new Pair<>(EventNotifier.class, new EventNotifier()),
                new Pair<>(TournamentNotifier.class, new TournamentNotifier()),
                new Pair<>(CompetitorNotifier.class, new CompetitorNotifier()),
                new Pair<>(JoinRequestNotifier.class, new JoinRequestNotifier())
        );
    }

    private static NotifierProvider getInstance() {
        if (ourInstance == null) ourInstance = new NotifierProvider();
        return ourInstance;
    }


    @SuppressWarnings("unchecked")
    public static <T extends Model<T>> Notifier<T> forModel(Class<? extends T> itemClass) {
        return (Notifier<T>) getInstance().singletonCache.forModel(itemClass);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Model<T>, R extends Notifier<T>> R forNotifier(Class<R> itemClass) {
        return (R) getInstance().singletonCache.forInstance(itemClass);
    }

    private static Notifier get(Class<? extends Notifier> unknown) {
        { Logger.log("NotifierProvider", "Dummy Notifier created for unrecognized class" + unknown.getName()); }
        return falseNotifier;
    }

    private static Notifier falseNotifier =  new Notifier() {
        @Override String getNotifyId() { return ""; }

        @SuppressWarnings("unchecked") @Override protected ModelRepo getRepository() { //noinspection unchecked
            return RepoProvider.falseRepo;
        }

        @Override
        protected NotificationChannel[] getNotificationChannels() { return new NotificationChannel[0]; }
    };
}
