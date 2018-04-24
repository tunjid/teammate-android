package com.mainstreetcode.teammate.adapters;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.SelectionViewHolder;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.enums.Position;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.getItemView;

/**
 * Adapter for {@link Role}
 */

public class JoinRequestAdapter extends BaseRecyclerViewAdapter<BaseItemViewHolder, JoinRequestAdapter.AdapterListener> {

    private final JoinRequest joinRequest;

    public JoinRequestAdapter(JoinRequest joinRequest, AdapterListener listener) {
        super(listener);
        this.joinRequest = joinRequest;
    }

    @NonNull
    @Override
    public BaseItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.INPUT:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), joinRequest::isEmpty);
            case Item.ROLE:
                return new SelectionViewHolder<>(getItemView(R.layout.viewholder_simple_input, viewGroup), R.string.choose_role, Config.getPositions(), Position::getName, Position::getCode, joinRequest::isEmpty);
            default:
                return new BaseItemViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseItemViewHolder baseItemViewHolder, int i) {
        baseItemViewHolder.bind(joinRequest.asItems().get(i));
    }

    @Override
    public int getItemCount() {
        return joinRequest.asItems().size();
    }

    @Override
    public int getItemViewType(int position) {
        return joinRequest.asItems().get(position).getItemType();
    }

    public interface AdapterListener extends ImageWorkerFragment.ImagePickerListener {}
}
