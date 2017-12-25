package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.Media;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.repository.MediaRepository;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;

public class MediaViewModel extends ViewModel {

    private final MediaRepository repository;

    public MediaViewModel() {
        repository = MediaRepository.getInstance();
    }

    public Flowable<Media> getMedia(Media model) {
        return repository.get(model);
    }

    public Flowable<DiffUtil.DiffResult> getTeamMedia(List<Media> source, Team team, Date date) {
        return Identifiable.diff(repository.getTeamMedia(team, date), () -> source, ModelUtils::preserveList);
    }

}
