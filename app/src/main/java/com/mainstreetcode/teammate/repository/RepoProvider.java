package com.mainstreetcode.teammate.repository;

import android.util.Pair;

import com.mainstreetcode.teammate.model.BlockedUser;
import com.mainstreetcode.teammate.model.Chat;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Device;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.Guest;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Media;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Prefs;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Stat;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.TeamMember;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.persistence.EntityDao;
import com.mainstreetcode.teammate.util.Logger;
import com.mainstreetcode.teammate.util.SingletonCache;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

public class RepoProvider {

    private static RepoProvider ourInstance;

    private final SingletonCache<Model, ModelRepo> singletonCache;

    private RepoProvider() {

        //noinspection unchecked
        singletonCache = new SingletonCache<Model, ModelRepo>(itemClass -> {
            if (itemClass.equals(User.class)) return UserRepo.class;
            if (itemClass.equals(Team.class)) return TeamRepo.class;
            if (itemClass.equals(Role.class)) return RoleRepo.class;
            if (itemClass.equals(Chat.class)) return ChatRepo.class;
            if (itemClass.equals(Game.class)) return GameRepo.class;
            if (itemClass.equals(Stat.class)) return StatRepo.class;
            if (itemClass.equals(Prefs.class)) return PrefsRepo.class;
            if (itemClass.equals(Media.class)) return MediaRepo.class;
            if (itemClass.equals(Guest.class)) return GuestRepo.class;
            if (itemClass.equals(Event.class)) return EventRepo.class;
            if (itemClass.equals(Config.class)) return ConfigRepo.class;
            if (itemClass.equals(Device.class)) return DeviceRepo.class;
            if (itemClass.equals(Tournament.class)) return TournamentRepo.class;
            if (itemClass.equals(Competitor.class)) return CompetitorRepo.class;
            if (itemClass.equals(BlockedUser.class)) return BlockedUserRepo.class;
            if (itemClass.equals(JoinRequest.class)) return JoinRequestRepo.class;
            if (itemClass.equals(TeamMember.class)) return  TeamMemberRepo.class;
            return UserRepo.class;
        },
                RepoProvider::get,
                new Pair<>(UserRepo.class, new UserRepo()),
                new Pair<>(TeamRepo.class, new TeamRepo()),
                new Pair<>(RoleRepo.class, new RoleRepo()),
                new Pair<>(ChatRepo.class, new ChatRepo()),
                new Pair<>(GameRepo.class, new GameRepo()),
                new Pair<>(StatRepo.class, new StatRepo()),
                new Pair<>(PrefsRepo.class, new PrefsRepo()),
                new Pair<>(MediaRepo.class, new MediaRepo()),
                new Pair<>(GuestRepo.class, new GuestRepo()),
                new Pair<>(EventRepo.class, new EventRepo()),
                new Pair<>(ConfigRepo.class, new ConfigRepo()),
                new Pair<>(DeviceRepo.class, new DeviceRepo()),
                new Pair<>(GameRoundRepo.class, new GameRoundRepo()),
                new Pair<>(TournamentRepo.class, new TournamentRepo()),
                new Pair<>(CompetitorRepo.class, new CompetitorRepo()),
                new Pair<>(BlockedUserRepo.class, new BlockedUserRepo()),
                new Pair<>(JoinRequestRepo.class, new JoinRequestRepo()),
                new Pair<>(TeamMemberRepo.class, new TeamMemberRepo())
        );
    }

    static RepoProvider getInstance() {
        if (ourInstance == null) ourInstance = new RepoProvider();
        return ourInstance;
    }

    public static boolean initialized() { return ourInstance != null; }

    @SuppressWarnings("unchecked")
    public static <T extends Model<T>, R extends ModelRepo<T>> R forRepo(Class<R> itemClass) {
        return (R) getInstance().singletonCache.forInstance(itemClass);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T extends Model<T>> ModelRepo<T> forModel(Class<T> itemClass) {
        return (ModelRepo<T>) getInstance().singletonCache.forModel(itemClass);
    }

    private static ModelRepo get(Class<? extends ModelRepo> unknown) {
        return new ModelRepo() {
            { Logger.log("RepoProvider", "Dummy Repo created for unrecognized class" + unknown.getName()); }

            @Override public EntityDao dao() { return EntityDao.daDont(); }

            @Override public Single createOrUpdate(Model model) { return Single.just(model); }

            @Override public Flowable get(String id) { return Flowable.empty(); }

            @Override public Single delete(Model model) { return Single.just(model); }

            @Override
            Function<List, List> provideSaveManyFunction() { return __ -> Collections.emptyList(); }
        };
    }

    public static final ModelRepo repo = new ModelRepo() {
        @Override public EntityDao dao() { return EntityDao.daDont(); }

        @Override public Single createOrUpdate(Model model) { return Single.just(model); }

        @Override public Flowable get(String id) { return Flowable.empty(); }

        @Override public Single delete(Model model) { return Single.just(model); }

        @Override
        Function<List, List> provideSaveManyFunction() { return __ -> Collections.emptyList(); }
    };
//
//    ModelRepo repo = null;
//    Map<Class<? extends ModelRepo>, ModelRepo<?>> repoMap = getInstance().map;
//
//        if (itemClass.equals(User.class)) repo = repoMap.get(UserRepo.class);
//        if (itemClass.equals(Team.class)) repo = repoMap.get(TeamRepo.class);
//        if (itemClass.equals(Role.class)) repo = repoMap.get(RoleRepo.class);
//        if (itemClass.equals(Chat.class)) repo = repoMap.get(ChatRepo.class);
//        if (itemClass.equals(Game.class)) repo = repoMap.get(GameRepo.class);
//        if (itemClass.equals(Stat.class)) repo = repoMap.get(StatRepo.class);
//        if (itemClass.equals(Prefs.class)) repo = repoMap.get(PrefsRepo.class);
//        if (itemClass.equals(Media.class)) repo = repoMap.get(MediaRepo.class);
//        if (itemClass.equals(Guest.class)) repo = repoMap.get(GuestRepo.class);
//        if (itemClass.equals(Event.class)) repo = repoMap.get(EventRepo.class);
//        if (itemClass.equals(Config.class)) repo = repoMap.get(ConfigRepo.class);
//        if (itemClass.equals(Device.class)) repo = repoMap.get(DeviceRepo.class);
//        if (itemClass.equals(Tournament.class)) repo = repoMap.get(TournamentRepo.class);
//        if (itemClass.equals(Competitor.class)) repo = repoMap.get(CompetitorRepo.class);
//        if (itemClass.equals(TeamMember.class)) repo = repoMap.get(TeamMemberRepo.class);
//        if (itemClass.equals(BlockedUser.class)) repo = repoMap.get(BlockedUserRepo.class);
//        if (itemClass.equals(JoinRequest.class)) repo = repoMap.get(JoinRequestRepo.class);
//
//        if (repo == null) repo = new ModelRepo() {
//        { Logger.log("RepoProvider", "Dummy Repo created for unrecognized class" + itemClass.getName()); }
//
//        @Override public EntityDao dao() { return EntityDao.daDont(); }
//
//        @Override public Single createOrUpdate(Model model) { return Single.just(model); }
//
//        @Override public Flowable get(String id) { return Flowable.empty(); }
//
//        @Override public Single delete(Model model) { return Single.just(model); }
//
//        @Override
//        Function<List, List> provideSaveManyFunction() { return __ -> Collections.emptyList(); }
//    };

//    map = new HashMap<>();
//
//        map.put(UserRepo.class, new UserRepo());
//        map.put(TeamRepo.class, new TeamRepo());
//        map.put(RoleRepo.class, new RoleRepo());
//        map.put(ChatRepo.class, new ChatRepo());
//        map.put(GameRepo.class, new GameRepo());
//        map.put(StatRepo.class, new StatRepo());
//        map.put(PrefsRepo.class, new PrefsRepo());
//        map.put(MediaRepo.class, new MediaRepo());
//        map.put(GuestRepo.class, new GuestRepo());
//        map.put(EventRepo.class, new EventRepo());
//        map.put(ConfigRepo.class, new ConfigRepo());
//        map.put(DeviceRepo.class, new DeviceRepo());
//        map.put(GameRoundRepo.class, new GameRoundRepo());
//        map.put(TournamentRepo.class, new TournamentRepo());
//        map.put(CompetitorRepo.class, new CompetitorRepo());
//        map.put(TeamMemberRepo.class, new TeamMemberRepo());
//        map.put(BlockedUserRepo.class, new BlockedUserRepo());
//        map.put(JoinRequestRepo.class, new JoinRequestRepo());
}
