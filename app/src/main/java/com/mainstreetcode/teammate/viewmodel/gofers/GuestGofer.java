package com.mainstreetcode.teammate.viewmodel.gofers;

import androidx.arch.core.util.Function;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Guest;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.util.TeammateException;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

public class GuestGofer extends TeamHostingGofer<Guest> {

    private final Function<Guest, Flowable<Guest>> getFunction;

    public GuestGofer(Guest model,
                      Consumer<Throwable> onError,
                      Function<Guest, Flowable<Guest>> getFunction) {
        super(model, onError);
        this.getFunction = getFunction;
        items.addAll(model.asItems());
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
    public Flowable<DiffUtil.DiffResult> fetch() {
        Flowable<List<Identifiable>> source = getFunction.apply(model).map(Guest::asIdentifiables);
        return Identifiable.diff(source, this::getItems, (itemsCopy, updated) -> updated);
    }

    Single<DiffUtil.DiffResult> upsert() {
        return Single.error(new TeammateException("Cannot upsert"));
    }

    public Completable delete() {
        return Completable.error(new TeammateException("Cannot delete"));
    }
}
