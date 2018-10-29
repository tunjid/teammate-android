package com.mainstreetcode.teammate.viewmodel.gofers;

import androidx.arch.core.util.Function;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.TeammateException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public class UserGofer extends Gofer<User> {

    private final Function<User, Boolean> authUserFunction;
    private final Function<User, Flowable<User>> getFunction;
    private final Function<User, Single<User>> updateFunction;

    public UserGofer(User model,
                     Function<User, Boolean> authUserFunction,
                     Function<User, Flowable<User>> getFunction,
                     Function<User, Single<User>> updateFunction) {
        super(model, ErrorHandler.EMPTY);
        this.authUserFunction = authUserFunction;
        this.getFunction = getFunction;
        this.updateFunction = updateFunction;
        items.addAll(filter(new ArrayList<>(model.asItems())));
    }

    @Override
    Flowable<Boolean> changeEmitter() {
        return Flowable.empty();
    }

    @Nullable
    @Override
    public String getImageClickMessage(Fragment fragment) {
        return null;
    }

    @Override
    public Flowable<DiffUtil.DiffResult> fetch() {
        Flowable<List<Identifiable>> listFlowable = getFunction.apply(model).map(User::asIdentifiables);
        return Identifiable.diff(listFlowable, this::getItems, (stale, updated) -> filter(updated));
    }

    Single<DiffUtil.DiffResult> upsert() {
        Single<List<Identifiable>> source = updateFunction.apply(model).map(User::asIdentifiables);
        return Identifiable.diff(source, this::getItems, (itemsCopy, updated) -> updated);
    }

    public Completable delete() {
        return Completable.error(new TeammateException("Cannot delete"));
    }

    private List<Identifiable> filter(List<Identifiable> list) {
        boolean isAuthUser = authUserFunction.apply(model);
        if (isAuthUser) return list;

        Iterator<Identifiable> it = list.iterator();

        while (it.hasNext()) {
            Identifiable next = it.next();
            if (!(next instanceof Item)) continue;
            if (((Item) next).getStringRes() == R.string.email) it.remove();
        }

        return list;
    }
}
