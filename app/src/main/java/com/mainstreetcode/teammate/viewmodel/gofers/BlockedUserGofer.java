package com.mainstreetcode.teammate.viewmodel.gofers;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.BlockedUser;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.util.TeammateException;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class BlockedUserGofer extends TeamHostingGofer<BlockedUser> {

    private final List<Item<BlockedUser>> items;
    private final Function<BlockedUser, Single<BlockedUser>> deleteFunction;

    public BlockedUserGofer(BlockedUser model,
                            Consumer<Throwable> onError,
                            Function<BlockedUser, Single<BlockedUser>> deleteFunction) {
        super(model, onError);
        this.deleteFunction = deleteFunction;
        this.items = new ArrayList<>(model.asItems());
    }

    public List<Item<BlockedUser>> getItems() {
        return items;
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
        return Single.error(new TeammateException("Unimplemented"));
    }

    public Completable delete() {
        return Single.defer(() -> deleteFunction.apply(model)).toCompletable();
    }
}
