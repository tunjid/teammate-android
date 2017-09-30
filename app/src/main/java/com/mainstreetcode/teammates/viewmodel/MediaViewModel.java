package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Media;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.repository.MediaRepository;
import com.mainstreetcode.teammates.util.ModelDiffCallback;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;

public class MediaViewModel extends ViewModel {

    private static final Comparator<Media> COMPARATOR = (a, b) -> a.getCreated().compareTo(b.getCreated());

    private final MediaRepository repository;

    public MediaViewModel() {
        repository = MediaRepository.getInstance();
    }

    public Flowable<DiffUtil.DiffResult> getTeamMedia(List<Media> source, Team team, Date date) {

        return repository.getTeamMedia(team, date).map(updatedMedia -> {
            List<Media> copy = new ArrayList<>(source);
            ModelUtils.preserveList(source, updatedMedia);
            Collections.sort(source, COMPARATOR);

            return DiffUtil.calculateDiff(new ModelDiffCallback<>(source, copy));
        });
    }

}
