package com.mainstreetcode.teammates.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.RoleSelectViewHolder;
import com.mainstreetcode.teammates.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammates.model.Item;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Role;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import java.util.List;

import static com.mainstreetcode.teammates.util.ViewHolderUtil.getItemView;

/**
 * Adapter for {@link Role}
 */

public class JoinRequestAdapter extends BaseRecyclerViewAdapter<BaseItemViewHolder, JoinRequestAdapter.AdapterListener> {

    private final JoinRequest joinRequest;
    private final List<String> roles;

    public JoinRequestAdapter(JoinRequest joinRequest, List<String> roles, AdapterListener listener) {
        super(listener);
        this.joinRequest = joinRequest;
        this.roles = roles;
    }

    @Override
    public BaseItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.INPUT:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), () -> true);
            case Item.ROLE:
                return new RoleSelectViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), roles, () -> true);
            default:
                return new BaseItemViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
        }
    }

    @Override
    public void onBindViewHolder(BaseItemViewHolder baseItemViewHolder, int i) {
        baseItemViewHolder.bind(joinRequest.get(i));
    }

    @Override
    public int getItemCount() {
        return joinRequest.size();
    }

    @Override
    public int getItemViewType(int position) {
        return joinRequest.get(position).getItemType();
    }

    public interface AdapterListener extends ImageWorkerFragment.ImagePickerListener {}
}
