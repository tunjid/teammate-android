package com.mainstreetcode.teammate.viewmodel.gofers;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Guest;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.util.TeammateException;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

public class GuestGofer extends Gofer<Guest> {

    private final List<Item<Guest>> items;

    public GuestGofer(Guest model, Consumer<Throwable> onError) {
        super(model, onError);
        this.items = new ArrayList<>(model.asItems());
    }

    public List<Item<Guest>> getItems() {
        return items;
    }

    @Override
    public Completable prepare() {
        return Completable.complete();
    }

    @Nullable
    @Override
    public String getImageClickMessage(Fragment fragment) {
        return fragment.getString(R.string.no_permission);
    }

    @Override
    public Flowable<DiffUtil.DiffResult> fetch() {
        return Flowable.empty();
    }

    Single<DiffUtil.DiffResult> upsert() {
        return Single.error(new TeammateException("Cannot upsert"));
    }

    public Completable delete() {
        return Completable.error(new TeammateException("Cannot delete"));
    }
}
