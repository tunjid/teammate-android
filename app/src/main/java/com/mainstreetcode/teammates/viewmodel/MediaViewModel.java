package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Media;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.repository.MediaRepository;
import com.mainstreetcode.teammates.util.ModelDiffCallback;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;

import static android.support.v7.util.DiffUtil.calculateDiff;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.computation;

public class MediaViewModel extends ViewModel {

    private final MediaRepository repository;

    public MediaViewModel() {
        repository = MediaRepository.getInstance();
    }

    public Flowable<Media> getMedia(Media model) {
        return repository.get(model);
    }

    public Flowable<DiffUtil.DiffResult> getTeamMedia(List<Media> source, Team team, Date date) {
        final List<Media> updated = new ArrayList<>(source);

        return repository.getTeamMedia(team, date)
                .concatMapDelayError(fetchedMedia -> Flowable.fromCallable(() -> {
                            List<Media> stale = new ArrayList<>(source);
                            ModelUtils.preserveList(updated, fetchedMedia);
                            return calculateDiff(new ModelDiffCallback(updated, stale));
                        })
                                .subscribeOn(computation())
                                .observeOn(mainThread())
                                .doOnNext(diffResult -> {
                                    source.clear();
                                    source.addAll(updated);
                                })
                );
    }

}
