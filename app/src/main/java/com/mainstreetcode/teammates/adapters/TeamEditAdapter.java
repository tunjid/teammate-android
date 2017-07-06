package com.mainstreetcode.teammates.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.ImageViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.RoleViewHolder;
import com.mainstreetcode.teammates.fragments.ImageWorkerFragment;
import com.mainstreetcode.teammates.model.Item;
import com.mainstreetcode.teammates.model.Team;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import java.util.List;

/**
 * Adapter for {@link Team}
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

public class TeamEditAdapter extends BaseRecyclerViewAdapter<BaseItemViewHolder, ImageWorkerFragment.ImagePickerListener> {

    private static final int PADDING = 11;

    private final Team team;
    private final List<String> roles;
    private final boolean isEditable;

    public TeamEditAdapter(Team team, List<String> roles, boolean isEditable, ImageWorkerFragment.ImagePickerListener listener) {
        super(listener);
        this.team = team;
        this.roles = roles;
        this.isEditable = isEditable;
    }

    @Override
    public BaseItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();

        @LayoutRes int layoutRes = viewType == Item.INPUT || viewType == Item.ROLE
                ? R.layout.viewholder_team_input
                : viewType == Item.IMAGE
                ? R.layout.viewholder_item_image
                : R.layout.view_holder_padding;

        View itemView = LayoutInflater.from(context).inflate(layoutRes, viewGroup, false);

        switch (viewType) {
            case Item.INPUT:
                return new InputViewHolder(itemView, isEditable);
            case Item.ROLE:
                return new RoleViewHolder(itemView, roles);
            case Item.IMAGE:
                return new ImageViewHolder(itemView, adapterListener);
            default:
                return new BaseItemViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(BaseItemViewHolder baseTeamViewHolder, int i) {
        if (i == team.size()) return;
        baseTeamViewHolder.bind(team.get(i));
    }

    @Override
    public int getItemCount() {
        return team.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == team.size() ? PADDING : team.get(position).getItemType();
    }
}
