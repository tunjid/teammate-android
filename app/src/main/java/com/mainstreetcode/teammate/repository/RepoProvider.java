package com.mainstreetcode.teammate.repository;

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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

public class RepoProvider {

    private static RepoProvider ourInstance;

    private final Map<Class<? extends ModelRepo>, ModelRepo<?>> repoMap;

    private RepoProvider() {
        repoMap = new HashMap<>();

        repoMap.put(UserRepo.class, new UserRepo());
        repoMap.put(TeamRepo.class, new TeamRepo());
        repoMap.put(RoleRepo.class, new RoleRepo());
        repoMap.put(ChatRepo.class, new ChatRepo());
        repoMap.put(GameRepo.class, new GameRepo());
        repoMap.put(StatRepo.class, new StatRepo());
        repoMap.put(PrefsRepo.class, new PrefsRepo());
        repoMap.put(MediaRepo.class, new MediaRepo());
        repoMap.put(GuestRepo.class, new GuestRepo());
        repoMap.put(EventRepo.class, new EventRepo());
        repoMap.put(ConfigRepo.class, new ConfigRepo());
        repoMap.put(DeviceRepo.class, new DeviceRepo());
        repoMap.put(GameRoundRepo.class, new GameRoundRepo());
        repoMap.put(TournamentRepo.class, new TournamentRepo());
        repoMap.put(CompetitorRepo.class, new CompetitorRepo());
        repoMap.put(TeamMemberRepo.class, new TeamMemberRepo());
        repoMap.put(BlockedUserRepo.class, new BlockedUserRepo());
        repoMap.put(JoinRequestRepo.class, new JoinRequestRepo());
    }

    public static RepoProvider getInstance() {
        if (ourInstance == null) ourInstance = new RepoProvider();
        return ourInstance;
    }

    public static boolean initialized() { return ourInstance != null; }

    @SuppressWarnings("unchecked")
    public static <T extends Model<T>, R extends ModelRepo<T>> R forRepo(Class<R> itemClass) {
        return (R) getInstance().repoMap.get(itemClass);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T extends Model<T>> ModelRepo<T> forModel(Class<T> itemClass) {

        ModelRepo repository = null;
        Map<Class<? extends ModelRepo>, ModelRepo<?>> repoMap = getInstance().repoMap;

        if (itemClass.equals(User.class)) repository = repoMap.get(UserRepo.class);
        if (itemClass.equals(Team.class)) repository = repoMap.get(TeamRepo.class);
        if (itemClass.equals(Role.class)) repository = repoMap.get(RoleRepo.class);
        if (itemClass.equals(Chat.class)) repository = repoMap.get(ChatRepo.class);
        if (itemClass.equals(Game.class)) repository = repoMap.get(GameRepo.class);
        if (itemClass.equals(Stat.class)) repository = repoMap.get(StatRepo.class);
        if (itemClass.equals(Prefs.class)) repository = repoMap.get(PrefsRepo.class);
        if (itemClass.equals(Media.class)) repository = repoMap.get(MediaRepo.class);
        if (itemClass.equals(Guest.class)) repository = repoMap.get(GuestRepo.class);
        if (itemClass.equals(Event.class)) repository = repoMap.get(EventRepo.class);
        if (itemClass.equals(Config.class)) repository = repoMap.get(ConfigRepo.class);
        if (itemClass.equals(Device.class)) repository = repoMap.get(DeviceRepo.class);
        if (itemClass.equals(Tournament.class)) repository = repoMap.get(TournamentRepo.class);
        if (itemClass.equals(Competitor.class)) repository = repoMap.get(CompetitorRepo.class);
        if (itemClass.equals(TeamMember.class)) repository = repoMap.get(TeamMemberRepo.class);
        if (itemClass.equals(BlockedUser.class)) repository = repoMap.get(BlockedUserRepo.class);
        if (itemClass.equals(JoinRequest.class)) repository = repoMap.get(JoinRequestRepo.class);

        if (repository == null) repository = new ModelRepo() {
            { Logger.log("RepoProvider", "Dummy Repo created for unrecognized class" + itemClass.getName()); }

            @Override public EntityDao dao() { return EntityDao.daDont(); }

            @Override public Single createOrUpdate(Model model) { return Single.just(model); }

            @Override public Flowable get(String id) { return Flowable.empty(); }

            @Override public Single delete(Model model) { return Single.just(model); }

            @Override
            Function<List, List> provideSaveManyFunction() { return __ -> Collections.emptyList(); }
        };

        return (ModelRepo<T>) repository;
    }
}
