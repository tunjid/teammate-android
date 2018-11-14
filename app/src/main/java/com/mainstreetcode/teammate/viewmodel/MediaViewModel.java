package com.mainstreetcode.teammate.viewmodel;

import androidx.core.util.Pair;
import androidx.recyclerview.widget.DiffUtil;

import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.MediaTransferIntentService;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Media;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.repository.MediaRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

public class MediaViewModel extends TeamMappedViewModel<Media> {

    private final MediaRepository repository;
    private final Map<Team, Set<Media>> selectionMap = new HashMap<>();

    public MediaViewModel() {
        repository = MediaRepository.getInstance();
    }

    @Override
    boolean hasNativeAds() {return false;}

    @Override
    Class<Media> valueClass() { return Media.class; }

    public Flowable<Media> getMedia(Media model) {
        return checkForInvalidObject(repository.get(model), model.getTeam(), model).cast(Media.class)
                .doOnNext(media -> {
                    if (media.isFlagged()) getModelList(media.getTeam()).remove(media);
                });
    }

    @Override
    Flowable<List<Media>> fetch(Team key, boolean fetchLatest) {
        return repository.modelsBefore(key, getQueryDate(fetchLatest, key, Media::getCreated));
    }

    public Maybe<Pair<Boolean, DiffUtil.DiffResult>> deleteMedia(Team team, boolean isAdmin) {
        AtomicBoolean partialDelete = new AtomicBoolean();
        List<Identifiable> source = getModelList(team);
        List<Media> toDelete = selectionMap.containsKey(team) ? new ArrayList<>(selectionMap.get(team)) : null;

        if (source == null || toDelete == null || toDelete.isEmpty()) return Maybe.empty();

        Flowable<List<Identifiable>> sourceFlowable = (isAdmin ? repository.privilegedDelete(team, toDelete) : repository.ownerDelete(toDelete))
                .toFlowable().map(this::toIdentifiable);

        return Identifiable.diff(sourceFlowable, () -> source, (sourceCopy, deleted) -> {
            partialDelete.set(deleted.size() != toDelete.size());
            sourceCopy.removeAll(deleted);
            return sourceCopy;
        })
                .map(diffResult -> new Pair<>(partialDelete.get(), diffResult))
                .firstElement()
                .doOnSuccess(diffResult -> clearSelections(team));
    }

    public boolean downloadMedia(Team team) {
        List<Identifiable> source = getModelList(team);
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
