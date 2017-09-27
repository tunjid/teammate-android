package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Media;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.repository.MediaRepository;
import com.mainstreetcode.teammates.util.ModelDiffCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Flowable;

public class MediaViewModel extends ViewModel {

    private static final Comparator<Media> COMPARATOR = (a, b) -> a.getCreated().compareTo(b.getCreated());

    private final MediaRepository repository;

    public MediaViewModel() {
        repository = MediaRepository.getInstance();
    }

    public Flowable<DiffUtil.DiffResult> getTeamMedia(List<Media> source, Team team, Date date) {

        return repository.getTeamMedia(team, date).map(media -> {
            List<Media> copy = new ArrayList<>(source);
            Set<Media> set = new HashSet<>(source);
            set.addAll(media);
            source.clear();
            source.addAll(set);
            Collections.sort(source, COMPARATOR);

            return DiffUtil.calculateDiff(new ModelDiffCallback<>(source, copy));
        });
    }

}
