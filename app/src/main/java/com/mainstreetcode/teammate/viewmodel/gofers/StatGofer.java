package com.mainstreetcode.teammate.viewmodel.gofers;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.Stat;
import com.mainstreetcode.teammate.util.Logger;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class StatGofer extends Gofer<Stat> {

    private final List<Item<Stat>> items;
    private final Function<Stat, Single<Stat>> upsertFunction;
    private final Function<Stat, Single<Stat>> deleteFunction;

    public StatGofer(Stat model, Consumer<Throwable> onError,
                     Function<Stat, Single<Stat>> upsertFunction, Function<Stat, Single<Stat>> deleteFunction) {
        super(model, onError);
        this.upsertFunction = upsertFunction;
        this.deleteFunction = deleteFunction;
        this.items = new ArrayList<>(model.asItems());
    }

    public List<Item<Stat>> getItems() {
        return items;
    }

    public boolean canEdit() {
        return true;
    }

    @Override
    public Completable prepare() {
        return Completable.complete();
    }

    @Nullable
    @Override
    public String getImageClickMessage(Fragment fragment) {
        return null;
    }

    @Override
    public Flowable<DiffUtil.DiffResult> fetch() {
        return Flowable.empty();
    }

    Single<DiffUtil.DiffResult> upsert() {
        Single<List<Item<Stat>>> source = Single.defer(() -> upsertFunction.apply(model)).map(Stat::asItems);
        return Identifiable.diff(source, this::getItems, (itemsCopy, updated) -> updated);
    }

    public Completable delete() {
        try {return deleteFunction.apply(model).toCompletable();}
        catch (Exception e) { Logger.log("Gopher", "", e);}
        return Completable.complete();
    }
}
