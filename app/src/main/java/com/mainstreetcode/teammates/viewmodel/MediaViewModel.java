package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammates.model.Media;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.repository.MediaRepository;

import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;

public class MediaViewModel extends ViewModel {

    private final MediaRepository repository;

    public MediaViewModel() {
        repository = MediaRepository.getInstance();
    }

    public Flowable<List<Media>> getTeamMedia(Team team) {
        return repository.getTeamMedia(team, new Date());
    }
}
