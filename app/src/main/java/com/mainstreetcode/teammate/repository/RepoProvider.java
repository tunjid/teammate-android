/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
            return falseRepo.getClass();
        },
                RepoProvider::get,
                new Pair<>(falseRepo.getClass(), falseRepo),
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
        Logger.log("RepoProvider", "Dummy Repo created for unrecognized class" + unknown.getName());
        return falseRepo;
    }

    public static final ModelRepo falseRepo = new ModelRepo() {
        @Override public EntityDao dao() { return EntityDao.daDont(); }

        @Override public Single createOrUpdate(Model model) { return Single.just(model); }

        @Override public Flowable get(String id) { return Flowable.empty(); }

        @Override public Single delete(Model model) { return Single.just(model); }

        @Override
        Function<List, List> provideSaveManyFunction() { return __ -> Collections.emptyList(); }
    };
}
