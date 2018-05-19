package com.mainstreetcode.teammate.viewmodel.gofers;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class RoleGofer extends TeamHostingGofer<Role> {

    private final List<Item<Role>> items;
    private final Function<Role, Flowable<Role>> getFunction;
    private final Function<Role, Single<Role>> deleteFunction;
    private final Function<Role, Single<Role>> updateFunction;

    public RoleGofer(Role model, Consumer<Throwable> onError, Function<Role, Flowable<Role>> getFunction, Function<Role, Single<Role>> deleteFunction, Function<Role, Single<Role>> updateFunction) {
        super(model, onError);
        this.getFunction = getFunction;
        this.deleteFunction = deleteFunction;
        this.updateFunction = updateFunction;
        this.items = new ArrayList<>(model.asItems());
    }

    public List<Item<Role>> getItems() {
        return items;
    }

    public boolean canChangeRoleFields() {
        return hasPrivilegedRole() || getSignedInUser().equals(model.getUser());
    }

    public String getDropRolePrompt(Fragment fragment) {
        User roleUser = model.getUser();
        return UserRepository.getInstance().getCurrentUser().equals(roleUser)
                ? fragment.getString(R.string.confirm_user_leave)
                : fragment.getString(R.string.confirm_user_drop, roleUser.getFirstName());
    }

    @Nullable
    @Override
    public String getImageClickMessage(Fragment fragment) {
        if (hasPrivilegedRole() || getSignedInUser().equals(model.getUser())) return null;
        return fragment.getString(R.string.no_permission);
    }

    @Override
    public Flowable<DiffUtil.DiffResult> fetch() {
        Flowable<List<Item<Role>>> source = Flowable.defer(() -> getFunction.apply(model)).map(Role::asItems);
        return Identifiable.diff(source, this::getItems, (itemsCopy, updated) -> updated);
    }

    Single<DiffUtil.DiffResult> upsert() {
        Single<List<Item<Role>>> source = Single.defer(() -> updateFunction.apply(model)).map(Role::asItems);
        return Identifiable.diff(source, this::getItems, (itemsCopy, updated) -> updated);
    }

    public Completable delete() {
        return Single.defer(() -> deleteFunction.apply(model)).toCompletable();
    }
}
