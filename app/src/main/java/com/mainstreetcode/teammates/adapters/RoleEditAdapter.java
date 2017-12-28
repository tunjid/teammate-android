package com.mainstreetcode.teammates.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.RoleSelectViewHolder;
import com.mainstreetcode.teammates.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammates.model.Item;
import com.mainstreetcode.teammates.model.Role;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import java.util.List;

import static com.mainstreetcode.teammates.util.ViewHolderUtil.getItemView;

/**
 * Adapter for {@link Role}
 */

public class RoleEditAdapter extends BaseRecyclerViewAdapter<BaseItemViewHolder, RoleEditAdapter.RoleEditAdapterListener> {

    private final Role role;
    private final List<String> roles;

    public RoleEditAdapter(Role role, List<String> roles, RoleEditAdapter.RoleEditAdapterListener listener) {
        super(listener);
        this.role = role;
        this.roles = roles;
    }

    @Override
    public BaseItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.INPUT:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), () -> false);
            case Item.ROLE:
                return new RoleSelectViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), roles, adapterListener::canChangeRole);
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

    public interface RoleEditAdapterListener extends ImageWorkerFragment.ImagePickerListener {
        boolean canChangeRole();
    }
}
