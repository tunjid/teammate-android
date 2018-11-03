package com.mainstreetcode.teammate.adapters;

import androidx.arch.core.util.Function;
import androidx.annotation.NonNull;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.SelectionViewHolder;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.enums.Position;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;

import java.util.List;

/**
 * Adapter for {@link Role}
 */

public class RoleEditAdapter extends InteractiveAdapter<BaseItemViewHolder, RoleEditAdapter.RoleEditAdapterListener> {

    private final List<Identifiable> items;

    public RoleEditAdapter(List<Identifiable> items, RoleEditAdapter.RoleEditAdapterListener listener) {
        super(listener);
        this.items = items;
    }

    @NonNull
    @Override
    public BaseItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.INPUT:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), Item.FALSE)
                        .setButtonRunnable((Function<Item, Boolean>) this::showsChangePicture, R.drawable.ic_picture_white_24dp, adapterListener::onImageClick);
            case Item.ABOUT:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), Item.FALSE, Item.ALL_INPUT_VALID);
            case Item.NICKNAME:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canChangeRoleFields, Item.ALL_INPUT_VALID);
            case Item.ROLE:
                return new SelectionViewHolder<>(getItemView(R.layout.viewholder_simple_input, viewGroup), R.string.choose_role, Config.getPositions(), Position::getName, Position::getCode, adapterListener::canChangeRolePosition);
            default:
                return new BaseItemViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseItemViewHolder baseItemViewHolder, int i) {
        Identifiable item = items.get(i);
        if (item instanceof Item) baseItemViewHolder.bind((Item) item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        Identifiable item = items.get(position);
        return item instanceof Item ? ((Item) item).getItemType() : Item.INPUT;
    }

    private boolean showsChangePicture(Item item) {
        return item.getStringRes() == R.string.first_name && adapterListener.canChangeRoleFields();
    }

    public interface RoleEditAdapterListener extends ImageWorkerFragment.ImagePickerListener {
        boolean canChangeRolePosition();

        boolean canChangeRoleFields();
    }
}
