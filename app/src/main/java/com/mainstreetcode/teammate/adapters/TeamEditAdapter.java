package com.mainstreetcode.teammate.adapters;

import androidx.arch.core.util.Function;
import androidx.annotation.NonNull;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.ClickInputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.SelectionViewHolder;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;

import java.util.List;

import static com.mainstreetcode.teammate.model.Item.ALL_INPUT_VALID;
import static com.mainstreetcode.teammate.model.Item.FALSE;

/**
 * Adapter for {@link Team}
 */

public class TeamEditAdapter extends InteractiveAdapter<BaseItemViewHolder, TeamEditAdapter.TeamEditAdapterListener> {

    private final List<Identifiable> items;

    public TeamEditAdapter(List<Identifiable> items, TeamEditAdapter.TeamEditAdapterListener listener) {
        super(listener);
        // setHasStableIds(true); DO NOT PUT THIS BACK
        this.items = items;
    }

    @NonNull
    @Override
    public BaseItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.ABOUT:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), FALSE);
            case Item.INFO:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEditFields, ViewHolderUtil.allowsSpecialCharacters);
            case Item.INPUT:
            case Item.NUMBER:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEditFields)
                        .setButtonRunnable((Function<Item, Boolean>) this::showsChangePicture, R.drawable.ic_picture_white_24dp, adapterListener::onImageClick);
            case Item.DESCRIPTION:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEditFields, ALL_INPUT_VALID);
            case Item.SPORT:
                return new SelectionViewHolder<>(getItemView(R.layout.viewholder_simple_input, viewGroup), R.string.choose_sport, Config.getSports(), Sport::getName, Sport::getCode, adapterListener::canEditFields, ALL_INPUT_VALID);
            case Item.CITY:
            case Item.STATE:
                return new ClickInputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEditFields, adapterListener::onAddressClicked);
            case Item.ZIP:
                return new ClickInputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEditFields, adapterListener::onAddressClicked, ALL_INPUT_VALID);
            default:
                return new BaseItemViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseItemViewHolder viewHolder, int i) {
        Identifiable item = items.get(i);
        if (item instanceof Item) viewHolder.bind((Item) item);
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
        return item.getStringRes() == R.string.team_name && adapterListener.canEditFields();
    }

    public interface TeamEditAdapterListener extends ImageWorkerFragment.ImagePickerListener {

        void onAddressClicked();

        boolean canEditFields();
    }
}
