package com.mainstreetcode.teammate.viewmodel.gofers;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.TeammateException;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

public class UserGofer extends Gofer<User> {

    private final List<Item<User>> items;
    private final Function<User, Flowable<User>> getFunction;
    private final Function<User, Single<User>> updateFunction;

    public UserGofer(User model, Function<User, Flowable<User>> getFunction, Function<User, Single<User>> updateFunction) {
        super(model, ErrorHandler.EMPTY);
        this.getFunction = getFunction;
        this.updateFunction = updateFunction;
        this.items = new ArrayList<>(model.asItems());
    }

    public List<Item<User>> getItems() {
        return items;
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
        Flowable<List<Item<User>>> listFlowable = Flowable.defer(() -> getFunction.apply(model)).map(User::asItems);
        return Identifiable.diff(listFlowable, this::getItems, (stale, updated) -> updated);
    }

    Single<DiffUtil.DiffResult> upsert() {
        Single<List<Item<User>>> source = Single.defer(() -> updateFunction.apply(model)).map(User::asItems);
        return Identifiable.diff(source, this::getItems, (itemsCopy, updated) -> updated);
    }

    public Completable delete() {
        return Completable.error(new TeammateException("Cannot delete"));
    }
}
