package com.mainstreetcode.teammate.adapters;

import android.arch.core.util.Function;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.ClickInputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.SelectionViewHolder;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import java.util.List;

import static com.mainstreetcode.teammate.model.Item.FALSE;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.getItemView;

/**
 * Adapter for {@link Team}
 */

public class TeamEditAdapter extends BaseRecyclerViewAdapter<BaseItemViewHolder, TeamEditAdapter.TeamEditAdapterListener> {

    private final List<Item<Team>> items;

    public TeamEditAdapter(List<Item<Team>> items, TeamEditAdapter.TeamEditAdapterListener listener) {
        super(listener);
        this.items = items;
    }

    @NonNull
    @Override
    public BaseItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.INFO:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), FALSE);
            case Item.INPUT:
            case Item.NUMBER:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEditFields)
                        .setButtonRunnable((Function<Item, Boolean>) this::showsChangePicture, R.drawable.ic_picture_white_24dp, adapterListener::onImageClick);
            case Item.DESCRIPTION:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEditFields, FALSE);
            case Item.SPORT:
                return new SelectionViewHolder<>(getItemView(R.layout.viewholder_simple_input, viewGroup), R.string.choose_sport, Config.getSports(), Sport::getName, Sport::getCode, adapterListener::canEditFields, FALSE);
            case Item.CITY:
            case Item.STATE:
                return new ClickInputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEditFields, adapterListener::onAddressClicked);
            case Item.ZIP:
                return new ClickInputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEditFields, adapterListener::onAddressClicked, () -> false);
            default:
                return new BaseItemViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseItemViewHolder baseTeamViewHolder, int i) {
        baseTeamViewHolder.bind(items.get(i));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getItemType();
    }

    private boolean showsChangePicture(Item item) {
        return item.getStringRes() == R.string.team_name && adapterListener.canEditFields();
    }

    public interface TeamEditAdapterListener extends ImageWorkerFragment.ImagePickerListener {

        void onAddressClicked();

        boolean canEditFields();
    }
}
