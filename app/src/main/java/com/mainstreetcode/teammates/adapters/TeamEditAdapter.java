package com.mainstreetcode.teammates.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.ClickInputViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.RoleSelectViewHolder;
import com.mainstreetcode.teammates.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammates.model.Item;
import com.mainstreetcode.teammates.model.Team;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import java.util.List;

import static com.mainstreetcode.teammates.util.ViewHolderUtil.getItemView;

/**
 * Adapter for {@link Team}
 */

public class TeamEditAdapter extends BaseRecyclerViewAdapter<BaseItemViewHolder, TeamEditAdapter.TeamEditAdapterListener> {


    private final Team team;
    private final List<String> roles;

    public TeamEditAdapter(Team team, List<String> roles, TeamEditAdapter.TeamEditAdapterListener listener) {
        super(listener);
        this.team = team;
        this.roles = roles;
    }

    @Override
    public BaseItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.INFO:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), () -> false);
            case Item.INPUT:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::isPrivileged);
            case Item.ROLE:
                return new RoleSelectViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), roles, adapterListener::isJoiningTeam);
            case Item.ADDRESS:
                return new ClickInputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::isPrivileged, adapterListener::onAddressClicked);
            default:
                return new BaseItemViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
        }
    }

    @Override
    public void onBindViewHolder(BaseItemViewHolder baseTeamViewHolder, int i) {
        baseTeamViewHolder.bind(team.get(i));
    }

    @Override
    public int getItemCount() {
        return adapterListener.isJoiningTeam() ? team.size() : team.size() - 1;
    }

    @Override
    public int getItemViewType(int position) {
        return team.get(position).getItemType();
    }

    public interface TeamEditAdapterListener extends ImageWorkerFragment.ImagePickerListener {
        void onAddressClicked();

        boolean isJoiningTeam();

        boolean isPrivileged();
    }
}
