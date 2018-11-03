package com.mainstreetcode.teammate.adapters;

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

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;

import static com.mainstreetcode.teammate.model.Item.ALL_INPUT_VALID;

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
            case Item.INFO:
            case Item.INPUT:
            case Item.NUMBER:
            case Item.DESCRIPTION:
                return new InputViewHolder(
                        getItemView(R.layout.viewholder_simple_input, viewGroup),
                        (Function<Item, Boolean>) this::canEdit,
                        (Function<Item, CharSequence>) this::textChecker
                ).setButtonRunnable(
                        R.drawable.ic_picture_white_24dp,
                        adapterListener::onImageClick,
                        (Function<Item, Boolean>) this::showsChangePicture);
            case Item.SPORT:
                return new SelectionViewHolder<>(
                        getItemView(R.layout.viewholder_simple_input, viewGroup),
                        R.string.choose_sport,
                        Config.getSports(),
                        Sport::getName,
                        Sport::getCode,
                        this::canEdit,
                        ALL_INPUT_VALID);
            case Item.CITY:
            case Item.STATE:
                return new ClickInputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup),
                        this::canEdit,
                        adapterListener::onAddressClicked);
            case Item.ZIP:
                return new ClickInputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup),
                        this::canEdit,
                        adapterListener::onAddressClicked, ALL_INPUT_VALID);
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

    private boolean canEdit(Item item) {
        switch (item.getItemType()) {
            default:
            case Item.ABOUT:
                return Item.FALSE.apply(item);
            case Item.ZIP:
            case Item.CITY:
            case Item.INFO:
            case Item.STATE:
            case Item.INPUT:
            case Item.SPORT:
            case Item.NUMBER:
            case Item.DESCRIPTION:
                return adapterListener.canEditFields();
        }
    }

    private CharSequence textChecker(Item item) {
        switch (item.getItemType()) {
            default:
            case Item.INPUT:
            case Item.NUMBER:
                return Item.NON_EMPTY.apply(item);
            case Item.INFO:
                return ViewHolderUtil.allowsSpecialCharacters.apply(item.getValue());
            case Item.DESCRIPTION:
                return Item.ALL_INPUT_VALID.apply(item);
        }
    }

    public interface TeamEditAdapterListener extends ImageWorkerFragment.ImagePickerListener {

        void onAddressClicked();

        boolean canEditFields();
    }
}
