package com.mainstreetcode.teammate.adapters;

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
import com.mainstreetcode.teammate.model.enums.Sport;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;

import java.util.List;

import static com.mainstreetcode.teammate.model.Item.ALL_INPUT_VALID;
import static com.mainstreetcode.teammate.model.Item.FALSE;

/**
 * Adapter for {@link Role}
 */

public class JoinRequestAdapter extends InteractiveAdapter<BaseItemViewHolder, JoinRequestAdapter.AdapterListener> {

    private final List<Identifiable> items;

    public JoinRequestAdapter(List<Identifiable> items, AdapterListener listener) {
        super(listener);
        this.items = items;
    }

    @NonNull
    @Override
    public BaseItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.SPORT:
                return new SelectionViewHolder<>(getItemView(R.layout.viewholder_simple_input, viewGroup),
                        R.string.choose_sport,
                        Config.getSports(),
                        Sport::getName,
                        Sport::getCode,
                        FALSE,
                        ALL_INPUT_VALID);
            case Item.ROLE:
                return new SelectionViewHolder<>(getItemView(R.layout.viewholder_simple_input, viewGroup),
                        R.string.choose_role,
                        Config.getPositions(),
                        Position::getName,
                        Position::getCode,
                        item -> adapterListener.canEditRole());
            case Item.ABOUT:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), Item.FALSE, ALL_INPUT_VALID);
            case Item.DESCRIPTION:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), Item.FALSE, ALL_INPUT_VALID);
            case Item.INPUT:
            default:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), item -> adapterListener.canEditFields());
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

    public interface AdapterListener extends ImageWorkerFragment.ImagePickerListener {
        boolean canEditFields();

        boolean canEditRole();
    }
}
