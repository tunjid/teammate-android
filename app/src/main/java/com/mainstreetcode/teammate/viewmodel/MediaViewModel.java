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
import com.mainstreetcode.teammate.MediaTransferIntentService;
import com.mainstreetcode.teammate.model.Media;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.repository.MediaRepo;
import com.mainstreetcode.teammate.repository.RepoProvider;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.FunctionalDiff;
import com.mainstreetcode.teammate.viewmodel.events.Alert;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.core.util.Pair;
import androidx.recyclerview.widget.DiffUtil;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.processors.PublishProcessor;

import static io.reactivex.schedulers.Schedulers.io;

public class MediaViewModel extends TeamMappedViewModel<Media> {

    private final MediaRepo repository;
    private final Map<Team, Set<Media>> selectionMap;
    private final PublishProcessor<DiffUtil.DiffResult> uploadCompletionProcessor;

    public MediaViewModel() {
        selectionMap = new HashMap<>();
        repository = RepoProvider.Companion.forRepo(MediaRepo.class);
        uploadCompletionProcessor = PublishProcessor.create();
    }

    @Override
    boolean hasNativeAds() {return false;}

    @Override
    Class<Media> valueClass() { return Media.class; }

    @Override
    void onModelAlert(Alert alert) {
        super.onModelAlert(alert);

        //noinspection unchecked,ResultOfMethodCallIgnored
        Alert.matches(alert, Alert.of(Alert.Creation.class, Media.class, media ->
                FunctionalDiff.of(Single.fromCallable(() -> media)
                        .subscribeOn(io())
                        .map(Collections::singletonList)
                        .map(this::toDifferentiable), getModelList(media.getTeam()), this::preserveList)
                        .subscribe(uploadCompletionProcessor::onNext, ErrorHandler.EMPTY)
        ));
    }

    @Override
    Flowable<List<Media>> fetch(Team key, boolean fetchLatest) {
        return repository.modelsBefore(key, getQueryDate(fetchLatest, key, Media::getCreated));
    }

    public Flowable<Media> getMedia(Media model) {
        return checkForInvalidObject(repository.get(model), model.getTeam(), model).cast(Media.class)
                .doOnNext(media -> {
                    if (media.isFlagged()) getModelList(media.getTeam()).remove(media);
                });
    }

    public Flowable<DiffUtil.DiffResult> listenForUploads() {
        return uploadCompletionProcessor;
    }

    public Maybe<Pair<Boolean, DiffUtil.DiffResult>> deleteMedia(Team team, boolean isAdmin) {
        AtomicBoolean partialDelete = new AtomicBoolean();
        List<Differentiable> source = getModelList(team);
        @SuppressWarnings("ConstantConditions")
        List<Media> toDelete = selectionMap.containsKey(team) ? new ArrayList<>(selectionMap.get(team)) : null;

        if (source == null || toDelete == null || toDelete.isEmpty()) return Maybe.empty();

        Flowable<List<Differentiable>> sourceFlowable = (isAdmin ? repository.privilegedDelete(team, toDelete) : repository.ownerDelete(toDelete))
                .toFlowable().map(this::toDifferentiable);

        return FunctionalDiff.of(sourceFlowable, source, (sourceCopy, deleted) -> {
            partialDelete.set(deleted.size() != toDelete.size());
            sourceCopy.removeAll(deleted);
            return sourceCopy;
        })
                .map(diffResult -> new Pair<>(partialDelete.get(), diffResult))
                .firstElement()
                .doOnSuccess(diffResult -> clearSelections(team));
    }

    public boolean downloadMedia(Team team) {
        List<Differentiable> source = getModelList(team);
        List<Media> toDownload = selectionMap.containsKey(team) ? new ArrayList<>(selectionMap.get(team)) : null;
        if (source == null || toDownload == null || toDownload.isEmpty()) return false;

        MediaTransferIntentService.startActionDownload(App.getInstance(), toDownload);
        clearSelections(team);
        return true;
    }

    public Single<Media> flagMedia(Media model) {
        return checkForInvalidObject(repository.flag(model).toFlowable(), model.getTeam(), model)
                .firstOrError().cast(Media.class).doOnSuccess(getModelList(model.getTeam())::remove);
    }

    public void clearSelections(Team team) {
        Set<Media> set = selectionMap.get(team);
        if (set != null) set.clear();
    }

    public boolean hasSelections(Team team) {
        return getNumSelected(team) != 0;
    }

    public int getNumSelected(Team team) {
        Set<Media> set = selectionMap.get(team);
        return set == null ? 0 : set.size();
    }

    public boolean isSelected(Media media) {
        Set<Media> set = selectionMap.get(media.getTeam());
        return set != null && set.contains(media);
    }

    public boolean select(Media media) {
        Set<Media> set = selectionMap.get(media.getTeam());
        if (set == null) {
            selectionMap.put(media.getTeam(), set = new HashSet<>());
            set.add(media);
            return true;
        }
        if (set.contains(media)) {
            set.remove(media);
            return false;
        }
        else {
            set.add(media);
            return true;
        }
    }

}
