package com.mainstreetcode.teammate.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.RoleSelectViewHolder;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.Role;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import java.util.List;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.getItemView;

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
