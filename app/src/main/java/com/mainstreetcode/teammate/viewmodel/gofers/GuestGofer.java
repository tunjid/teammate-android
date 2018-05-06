package com.mainstreetcode.teammate.viewmodel.gofers;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Guest;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.util.TeammateException;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class GuestGofer extends TeamHostingGofer<Guest> {

    private final List<Item<Guest>> items;
    private final Function<Guest, Flowable<Guest>> getFunction;

    public GuestGofer(Guest model,
                      Consumer<Throwable> onError,
                      Function<Guest, Flowable<Guest>> getFunction) {
        super(model, onError);
        this.items = new ArrayList<>(model.asItems());
        this.getFunction = getFunction;
    }

    public List<Item<Guest>> getItems() {
        return items;
    }

    @Nullable
    @Override
    public String getImageClickMessage(Fragment fragment) {
        return fragment.getString(R.string.no_permission);
    }

    public boolean canBlockUser() {
        return hasPrivilegedRole() && !getSignedInUser().equals(model.getUser());
    }

    @Override
    public Completable prepare() {
        return Completable.complete();
    }

    @Override
    public Flowable<DiffUtil.DiffResult> fetch() {
        Flowable<List<Item<Guest>>> source = Flowable.defer(() -> getFunction.apply(model)).map(Guest::asItems);
        return Identifiable.diff(source, () -> items, (itemsCopy, updated) -> updated);
    }

    Single<DiffUtil.DiffResult> upsert() {
        return Single.error(new TeammateException("Cannot upsert"));
    }

    public Completable delete() {
        return Completable.error(new TeammateException("Cannot delete"));
    }
}
