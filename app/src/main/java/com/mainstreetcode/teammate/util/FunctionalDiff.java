package com.mainstreetcode.teammate.util;


import android.os.HandlerThread;

import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Guest;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Media;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.TeamMember;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.notifications.FeedItem;
import com.tunjid.androidbootstrap.functions.BiFunction;
import com.tunjid.androidbootstrap.functions.collections.Lists;
import com.tunjid.androidbootstrap.recyclerview.diff.Diff;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.util.Comparator;
import java.util.List;

import androidx.recyclerview.widget.DiffUtil;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public class FunctionalDiff {

    private static final HandlerThread diffThread;

    static {
        diffThread = new HandlerThread("Diffing");
        diffThread.start();
    }

    public static <T extends Differentiable> Flowable<DiffUtil.DiffResult> of(Flowable<List<T>> sourceFlowable,
                                                                       List<T> original,
                                                                       BiFunction<List<T>, List<T>, List<T>> accumulator) {

        return sourceFlowable.concatMapDelayError(list -> Flowable.fromCallable(() -> Diff.calculate(original, list, accumulator))
                .subscribeOn(AndroidSchedulers.from(diffThread.getLooper()))
                .observeOn(mainThread())
                .doOnNext(diff -> Lists.replace(original, diff.cumulative))
                .map(diff -> diff.result));
    }

    public static <T extends Differentiable> Single<DiffUtil.DiffResult> of(Single<List<T>> sourceSingle,
                                                                     List<T> original,
                                                                     BiFunction<List<T>, List<T>, List<T>> accumulator) {

        return sourceSingle.flatMap(list -> Single.fromCallable(() -> Diff.calculate(original, list, accumulator))
                .subscribeOn(AndroidSchedulers.from(diffThread.getLooper()))
                .observeOn(mainThread())
                .doOnSuccess(diff -> Lists.replace(original, diff.cumulative))
                .map(diff -> diff.result));
    }

    @SuppressWarnings("unchecked")
   public static final Comparator<Differentiable> COMPARATOR = (modelA, modelB) -> {
        int pointsA = getPoints(modelA);
        int pointsB = getPoints(modelB);

        int a, b, modelComparison;
        a = b = modelComparison = Integer.compare(pointsA, pointsB);


        if (!isComparable(modelA, modelB)) return modelComparison;
        else a += ((Comparable) modelA).compareTo(modelB);

        return Integer.compare(a, b);
    };

   public static final Comparator<Differentiable> DESCENDING_COMPARATOR = (modelA, modelB) -> -COMPARATOR.compare(modelA, modelB);

    private static int getPoints(Differentiable identifiable) {
        if (identifiable instanceof FeedItem)
            identifiable = ((FeedItem) identifiable).getModel();
        if (identifiable instanceof TeamMember)
            identifiable = ((TeamMember) identifiable).getWrappedModel();
        if (identifiable.getClass().equals(Item.class)) return 0;
        if (identifiable.getClass().equals(JoinRequest.class)) return 5;
        if (identifiable.getClass().equals(Competitor.class)) return 10;
        if (identifiable.getClass().equals(Role.class)) return 15;
        if (identifiable.getClass().equals(Event.class)) return 20;
        if (identifiable.getClass().equals(Media.class)) return 25;
        if (identifiable.getClass().equals(Team.class)) return 30;
        if (identifiable.getClass().equals(User.class)) return 35;
        if (identifiable.getClass().equals(Guest.class)) return 40;
        return 0;
    }

    private static boolean isComparable(Differentiable modelA, Differentiable modelB) {
        return modelA instanceof Comparable
                && modelB instanceof Comparable
                && modelA.getClass().equals(modelB.getClass());
    }
}
