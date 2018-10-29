package com.mainstreetcode.teammate.viewmodel.gofers;

import androidx.arch.core.util.Function;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.BlockedUser;
import com.mainstreetcode.teammate.util.TeammateException;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

public class BlockedUserGofer extends TeamHostingGofer<BlockedUser> {

    private final Function<BlockedUser, Single<BlockedUser>> deleteFunction;

    public BlockedUserGofer(BlockedUser model,
                            Consumer<Throwable> onError,
                            Function<BlockedUser, Single<BlockedUser>> deleteFunction) {
        super(model, onError);
        this.deleteFunction = deleteFunction;
        items.addAll(model.asItems());
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
        return deleteFunction.apply(model).toCompletable();
    }
}
