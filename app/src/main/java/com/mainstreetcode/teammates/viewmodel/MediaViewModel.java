package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.Media;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.repository.MediaRepository;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Flowable;

public class MediaViewModel extends ViewModel {

    private final MediaRepository repository;
    private final Map<Team, List<Media>> mediaMap = new HashMap<>();
    private final Map<Team, Set<Media>> selectionMap = new HashMap<>();

    public MediaViewModel() {
        repository = MediaRepository.getInstance();
    }

    public Flowable<Media> getMedia(Media model) {
        return repository.get(model).doOnError(throwable -> ModelUtils.checkForInvalidObject(throwable, model, getMediaList(model.getTeam())));
    }

    public Flowable<DiffUtil.DiffResult> getTeamMedia(List<Media> source, Team team, Date date) {
        return Identifiable.diff(repository.getTeamMedia(team, date), () -> source, ModelUtils::preserveList);
    }

    public List<Media> getMediaList(Team team) {
        List<Media> media = mediaMap.get(team);
        if (!mediaMap.containsKey(team)) mediaMap.put(team, media = new ArrayList<>());

        return media;
    }

    public void clearSelections(Team team) {
        Set<Media> set = selectionMap.get(team);
        if (set != null) set.clear();
    }

    public boolean hasSelections(Team team) {
        Set<Media> set = selectionMap.get(team);
        return set != null && set.size() > 0;
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
