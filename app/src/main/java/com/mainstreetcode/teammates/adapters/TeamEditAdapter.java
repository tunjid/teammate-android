package com.mainstreetcode.teammates.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.ClickInputViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.ImageViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.RoleSelectViewHolder;
import com.mainstreetcode.teammates.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammates.model.Item;
import com.mainstreetcode.teammates.model.Team;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import java.util.List;

/**
 * Adapter for {@link Team}
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

public class TeamEditAdapter extends BaseRecyclerViewAdapter<BaseItemViewHolder, TeamEditAdapter.TeamEditAdapterListener> {


    private final Team team;
    private final List<String> roles;
    private final boolean isEditable;

    public TeamEditAdapter(Team team, List<String> roles, boolean isEditable, TeamEditAdapter.TeamEditAdapterListener listener) {
        super(listener);
        this.team = team;
        this.roles = roles;
        this.isEditable = isEditable;
    }

    @Override
    public BaseItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();

        @LayoutRes int layoutRes = viewType == Item.INPUT || viewType == Item.ROLE || viewType == Item.ADDRESS
                ? R.layout.viewholder_simple_input
                : viewType == Item.IMAGE
                ? R.layout.viewholder_item_image
                : R.layout.view_holder_padding;

        View itemView = LayoutInflater.from(context).inflate(layoutRes, viewGroup, false);

        switch (viewType) {
            case Item.INPUT:
                return new InputViewHolder(itemView, isEditable);
            case Item.ROLE:
                return new RoleSelectViewHolder(itemView, roles);
            case Item.IMAGE:
                return new ImageViewHolder(itemView, adapterListener);
            case Item.ADDRESS:
                return new ClickInputViewHolder(itemView, adapterListener);
            default:
                return new BaseItemViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(BaseItemViewHolder baseTeamViewHolder, int i) {
        baseTeamViewHolder.bind(team.get(i));
    }

    @Override
    public int getItemCount() {
        return team.size();
    }

    @Override
    public int getItemViewType(int position) {
        return team.get(position).getItemType();
    }

    public interface TeamEditAdapterListener extends ImageWorkerFragment.ImagePickerListener {
        void onAddressClicked();
    }
}
