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

package com.mainstreetcode.teammate.viewmodel;

import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Chat;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.notifications.ChatNotifier;
import com.mainstreetcode.teammate.notifications.NotifierProvider;
import com.mainstreetcode.teammate.repository.ChatRepo;
import com.mainstreetcode.teammate.repository.RepoProvider;
import com.mainstreetcode.teammate.util.Logger;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.io.EOFException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.arch.core.util.Function;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.socket.engineio.client.EngineIOException;

import static com.mainstreetcode.teammate.util.ModelUtils.fullPrinter;
import static com.tunjid.androidbootstrap.functions.collections.Lists.findFirst;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;


public class ChatViewModel extends TeamMappedViewModel<Chat> {

    private static final String XHR_POST_ERROR = "xhr post error";

    private final ChatRepo repository;
    private final ChatNotifier notifier;

    public ChatViewModel() {
        repository = RepoProvider.forRepo(ChatRepo.class);
        notifier = NotifierProvider.forNotifier(ChatNotifier.class);
    }

    @Override
    boolean hasNativeAds() {return false;}

    @Override
    boolean sortsAscending() {return true;}

    @Override
    Class<Chat> valueClass() { return Chat.class; }

    public void updateLastSeen(Team team) {
        repository.updateLastSeen(team);

        Chat chat = findFirst(getModelList(team), Chat.class);
        if (chat != null) clearNotifications(chat);
    }

    public String onScrollPositionChanged(Team team, int position) {
        Differentiable item = getModelList(team).get(position);
        if (!(item instanceof Chat)) return "";

        Date created = ((Chat) item).getCreated();
        Calendar then = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        then.setTime(created);

        boolean isToday = !ModelUtils.areDifferentDays(created, now.getTime());
        boolean isYesterday = !isToday && now.get(Calendar.DAY_OF_MONTH) - then.get(Calendar.DAY_OF_MONTH) == 1
                && now.get(Calendar.MONTH) == then.get(Calendar.MONTH)
                && now.get(Calendar.YEAR) == then.get(Calendar.YEAR);

        return isToday
                ? ""
                : isYesterday
                ? App.getInstance().getString(R.string.chat_yesterday)
                : fullPrinter.format(created);
    }

    public Flowable<Chat> listenForChat(Team team) {
        return repository.listenForChat(team)
                .onErrorResumeNext(listenRetryFunction(team)::apply)
                .doOnSubscribe(subscription -> notifier.setChatVisibility(team, true))
                .doFinally(() -> notifier.setChatVisibility(team, false))
                .observeOn(mainThread());
    }

    public Single<Chat> post(Chat chat) {
        return repository.createOrUpdate(chat).observeOn(mainThread());
    }

    @Override
    Flowable<List<Chat>> fetch(Team key, boolean fetchLatest) {
        return repository.modelsBefore(key, getQueryDate(fetchLatest, key, Chat::getCreated));
    }

    private Function<Throwable, Flowable<Chat>> listenRetryFunction(Team team) {
        return (throwable -> shouldRetry(throwable)
                ? repository.listenForChat(team)
                .onErrorResumeNext(listenRetryFunction(team)::apply)
                .doOnSubscribe(subscription -> notifier.setChatVisibility(team, true))
                .doFinally(() -> notifier.setChatVisibility(team, false))
                .observeOn(mainThread())
                : Flowable.error(throwable)
        );
    }

    private boolean shouldRetry(Throwable throwable) {
        boolean retry = XHR_POST_ERROR.equals(throwable.getMessage()) ||
                (throwable instanceof EngineIOException && throwable.getCause() instanceof EOFException);
        if (retry) Logger.log("CHAT", "Retrying because of predictable error", throwable);
        return retry;
    }

    @Override
    Date getQueryDate(boolean fetchLatest, Team key, Function<Chat, Date> dateFunction) {
        if (fetchLatest) return null;

        // Chats use find first
        Chat value = findFirst(getModelList(key), valueClass());
        return value == null ? null : dateFunction.apply(value);
    }
}
