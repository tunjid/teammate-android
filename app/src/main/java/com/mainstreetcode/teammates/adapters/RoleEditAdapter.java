package com.mainstreetcode.teammates.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.HeaderedImageViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.RoleSelectViewHolder;
import com.mainstreetcode.teammates.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammates.model.Item;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.User;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import java.util.List;

import io.reactivex.functions.BooleanSupplier;

import static com.mainstreetcode.teammates.util.ViewHolderUtil.getItemView;

/**
 * Adapter for {@link User}
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

public class RoleEditAdapter extends BaseRecyclerViewAdapter<BaseItemViewHolder, ImageWorkerFragment.ImagePickerListener> {

    private final Role role;
    private final List<String> roles;
    private final BooleanSupplier roleCheckSupplier;

    public RoleEditAdapter(Role role, List<String> roles,
                           BooleanSupplier roleCheckSupplier,
                           ImageWorkerFragment.ImagePickerListener listener) {
        super(listener);
        this.role = role;
        this.roles = roles;
        this.roleCheckSupplier = roleCheckSupplier;
    }

    @Override
    public BaseItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.INPUT:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), false);
            case Item.ROLE:
                return new RoleSelectViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), roles, canEditRole());
            case Item.IMAGE:
                return new HeaderedImageViewHolder(getItemView(R.layout.viewholder_item_image, viewGroup), adapterListener);
            default:
                return new BaseItemViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
        }
    }

    @Override
    public void onBindViewHolder(BaseItemViewHolder baseItemViewHolder, int i) {
        baseItemViewHolder.bind(role.get(i));
    }

    @Override
    public int getItemCount() {
        return role.size();
    }

    @Override
    public int getItemViewType(int position) {
        return role.get(position).getItemType();
    }

    private boolean canEditRole() {
        try {return roleCheckSupplier.getAsBoolean();}
        catch (Exception e) { return false;}
    }
}
