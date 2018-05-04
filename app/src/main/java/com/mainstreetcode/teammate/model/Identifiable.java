package com.mainstreetcode.teammate.model;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammate.notifications.FeedItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.BiFunction;

import static android.support.v7.util.DiffUtil.calculateDiff;
import static com.mainstreetcode.teammate.model.Identifiable.Util.getPoints;
import static com.mainstreetcode.teammate.model.Identifiable.Util.isComparable;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.computation;

public interface Identifiable {

    String getId();

    default boolean areContentsTheSame(Identifiable other) {
        return getId().equals(other.getId());
    }

    default Object getChangePayload(Identifiable other) {
        return null;
    }

    static <T extends Identifiable> Flowable<DiffUtil.DiffResult> diff(Flowable<List<T>> sourceFlowable,
                                                                       Callable<List<T>> sourceSupplier,
                                                                       BiFunction<List<T>, List<T>, List<T>> accumulator) {

        return Util.diff(sourceFlowable, sourceSupplier, accumulator);
    }

    static <T extends Identifiable> Single<DiffUtil.DiffResult> diff(Single<List<T>> sourceSingle,
                                                                     Callable<List<T>> sourceSupplier,
                                                                     BiFunction<List<T>, List<T>, List<T>> accumulator) {

        return Util.diff(sourceSingle, sourceSupplier, accumulator);
    }

    static <T extends Identifiable> Maybe<DiffUtil.DiffResult> diff(Maybe<List<T>> sourceSingle,
                                                                     Callable<List<T>> sourceSupplier,
                                                                     BiFunction<List<T>, List<T>, List<T>> accumulator) {

        return Util.diff(sourceSingle, sourceSupplier, accumulator);
    }

    class IdentifiableDiffCallback extends DiffUtil.Callback {

        private final List<? extends Identifiable> stale;
        private final List<? extends Identifiable> updated;

        IdentifiableDiffCallback(List<? extends Identifiable> updated, List<? extends Identifiable> stale) {
            this.updated = updated;
            this.stale = stale;
        }

        @Override
        public int getOldListSize() {
            return stale.size();
        }

        @Override
        public int getNewListSize() {
            return updated.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return stale.get(oldItemPosition).getId().equals(updated.get(newItemPosition).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return stale.get(oldItemPosition).areContentsTheSame(updated.get(newItemPosition));
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            return stale.get(oldItemPosition).getChangePayload(updated.get(newItemPosition));
        }
    }

    @SuppressWarnings("unchecked")
    Comparator<Identifiable> COMPARATOR = (modelA, modelB) -> {
        int pointsA = getPoints(modelA);
        int pointsB = getPoints(modelB);

        int a, b, modelComparison;
        a = b = modelComparison = Integer.compare(pointsA, pointsB);


        if (!isComparable(modelA, modelB)) return modelComparison;
        else a += ((Comparable) modelA).compareTo(modelB);

        return Integer.compare(a, b);
    };


    class Util {
        static int getPoints(Identifiable identifiable) {
            if (identifiable instanceof FeedItem)
                identifiable = ((FeedItem) identifiable).getModel();
            if (identifiable instanceof TeamMember)
                identifiable = ((TeamMember) identifiable).getWrappedModel();
            if (identifiable.getClass().equals(Item.class)) return 0;
            if (identifiable.getClass().equals(JoinRequest.class)) return 5;
            if (identifiable.getClass().equals(Role.class)) return 10;
            if (identifiable.getClass().equals(Event.class)) return 15;
            if (identifiable.getClass().equals(Media.class)) return 20;
            if (identifiable.getClass().equals(Team.class)) return 25;
            if (identifiable.getClass().equals(Guest.class)) return 30;
            return 0;
        }

        static boolean isComparable(Identifiable modelA, Identifiable modelB) {
            return modelA instanceof Comparable
                    && modelB instanceof Comparable
                    && modelA.getClass().equals(modelB.getClass());
        }

        static <T extends Identifiable> Flowable<DiffUtil.DiffResult> diff(Flowable<List<T>> sourceFlowable,
                                                                           Callable<List<T>> sourceSupplier,
                                                                           BiFunction<List<T>, List<T>, List<T>> accumulator) {

            AtomicReference<List<T>> sourceUpdater = new AtomicReference<>();

            return sourceFlowable.concatMapDelayError(fetchedItems -> Flowable.fromCallable(() -> getDiffResult(sourceSupplier, accumulator, sourceUpdater, fetchedItems))
                            .subscribeOn(computation())
                            .observeOn(mainThread())
                            .doOnNext(diffResult -> updateListOnUIThread(sourceSupplier, sourceUpdater))
            );
        }

        static <T extends Identifiable> Single<DiffUtil.DiffResult> diff(Single<List<T>> sourceSingle,
                                                                         Callable<List<T>> sourceSupplier,
                                                                         BiFunction<List<T>, List<T>, List<T>> accumulator) {

            AtomicReference<List<T>> sourceUpdater = new AtomicReference<>();

            return sourceSingle.flatMap(fetchedItems -> Single.fromCallable(() -> getDiffResult(sourceSupplier, accumulator, sourceUpdater, fetchedItems))
                            .subscribeOn(computation())
                            .observeOn(mainThread())
                            .doOnSuccess(diffResult -> updateListOnUIThread(sourceSupplier, sourceUpdater))
            );
        }

        static <T extends Identifiable> Maybe<DiffUtil.DiffResult> diff(Maybe<List<T>> sourceSingle,
                                                                        Callable<List<T>> sourceSupplier,
                                                                        BiFunction<List<T>, List<T>, List<T>> accumulator) {

            AtomicReference<List<T>> sourceUpdater = new AtomicReference<>();

            return sourceSingle.flatMap(fetchedItems -> Maybe.fromCallable(() -> getDiffResult(sourceSupplier, accumulator, sourceUpdater, fetchedItems))
                    .subscribeOn(computation())
                    .observeOn(mainThread())
                    .doOnSuccess(diffResult -> updateListOnUIThread(sourceSupplier, sourceUpdater))
            );
        }

        @NonNull
        private static <T extends Identifiable> DiffUtil.DiffResult getDiffResult(Callable<List<T>> sourceSupplier, BiFunction<List<T>, List<T>, List<T>> accumulator, AtomicReference<List<T>> sourceUpdater, List<T> fetchedItems) throws Exception {
            List<T> stale = sourceSupplier.call();
            List<T> updated = accumulator.apply(new ArrayList<>(stale), fetchedItems);

            sourceUpdater.set(updated);

            return calculateDiff(new IdentifiableDiffCallback(updated, stale));
        }

        private static <T extends Identifiable> void updateListOnUIThread(Callable<List<T>> sourceSupplier, AtomicReference<List<T>> sourceUpdater) throws Exception {
            List<T> source = sourceSupplier.call();
            source.clear();
            source.addAll(sourceUpdater.get());
        }
    }
}
