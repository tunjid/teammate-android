package com.mainstreetcode.teammate.viewmodel.gofers;

import androidx.arch.core.util.Function;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.util.FunctionalDiff;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.repository.UserRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

public class RoleGofer extends TeamHostingGofer<Role> {

    private final Function<Role, Flowable<Role>> getFunction;
    private final Function<Role, Single<Role>> deleteFunction;
    private final Function<Role, Single<Role>> updateFunction;

    public RoleGofer(Role model, Consumer<Throwable> onError, Function<Role, Flowable<Role>> getFunction, Function<Role, Single<Role>> deleteFunction, Function<Role, Single<Role>> updateFunction) {
        super(model, onError);
        this.getFunction = getFunction;
        this.deleteFunction = deleteFunction;
        this.updateFunction = updateFunction;
        this.items.addAll(model.asItems());
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
        Flowable<List<Differentiable>> source = getFunction.apply(model).map(Role::asDifferentiables);
        return FunctionalDiff.of(source, getItems(), (itemsCopy, updated) -> updated);
    }

    Single<DiffUtil.DiffResult> upsert() {
        Single<List<Differentiable>> source = updateFunction.apply(model).map(Role::asDifferentiables);
        return FunctionalDiff.of(source, getItems(), (itemsCopy, updated) -> updated);
    }

    public Completable delete() {
        return deleteFunction.apply(model).toCompletable();
    }
}
