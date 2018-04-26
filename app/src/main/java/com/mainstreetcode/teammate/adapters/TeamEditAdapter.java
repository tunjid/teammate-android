package com.mainstreetcode.teammate.adapters;

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

import static com.mainstreetcode.teammate.model.Item.FALSE;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.getItemView;

/**
 * Adapter for {@link Team}
 */

public class TeamEditAdapter extends BaseRecyclerViewAdapter<BaseItemViewHolder, TeamEditAdapter.TeamEditAdapterListener> {

    private final Team team;

    public TeamEditAdapter(Team team, TeamEditAdapter.TeamEditAdapterListener listener) {
        super(listener);
        this.team = team;
    }

    @NonNull
    @Override
    public BaseItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.INFO:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), FALSE);
            case Item.INPUT:
            case Item.NUMBER:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::isPrivileged);
            case Item.DESCRIPTION:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::isPrivileged, FALSE);
            case Item.SPORT:
                return new SelectionViewHolder<>(getItemView(R.layout.viewholder_simple_input, viewGroup), R.string.choose_sport, Config.getSports(), Sport::getName, Sport::getCode, adapterListener::isPrivileged, FALSE);
            case Item.CITY:
            case Item.STATE:
                return new ClickInputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::isPrivileged, adapterListener::onAddressClicked);
            case Item.ZIP:
                return new ClickInputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::isPrivileged, adapterListener::onAddressClicked, () -> false);
            default:
                return new BaseItemViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseItemViewHolder baseTeamViewHolder, int i) {
        baseTeamViewHolder.bind(team.asItems().get(i));
    }

    @Override
    public int getItemCount() {
        return team.asItems().size();
    }

    @Override
    public int getItemViewType(int position) {
        return team.asItems().get(position).getItemType();
    }

    public interface TeamEditAdapterListener extends ImageWorkerFragment.ImagePickerListener {
        void onAddressClicked();

        boolean isPrivileged();
    }
}
