package com.mainstreetcode.teammate.adapters;

import android.content.res.Resources;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.ContentAdViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.InstallAdViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.TeamViewHolder;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

import java.util.List;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.CONTENT_AD;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.INSTALL_AD;

public class TeamSearchAdapter extends TeamAdapter {

    public TeamSearchAdapter(List<Identifiable> items, AdapterListener listener) {
        super(items, listener);
    }

    @NonNull
    @Override
    public InteractiveViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return viewType == CONTENT_AD
                ? new ContentAdViewHolder(getItemView(R.layout.viewholder_grid_content_ad, viewGroup), adapterListener)
                : viewType == INSTALL_AD
                ? new InstallAdViewHolder(getItemView(R.layout.viewholder_grid_install_ad, viewGroup), adapterListener)
                : getTeamViewHolder(viewGroup);
    }

    @NonNull
    private TeamViewHolder getTeamViewHolder(@NonNull ViewGroup viewGroup) {
        View itemView = getItemView(R.layout.viewholder_list_item, viewGroup);
        Resources resources = itemView.getResources();
        ViewGroup.MarginLayoutParams params = ViewHolderUtil.getLayoutParams(itemView);
        params.leftMargin = params.rightMargin = resources.getDimensionPixelSize(R.dimen.half_margin);
        return new TeamViewHolder(itemView, adapterListener);
    }
}
