package com.mainstreetcode.teammates.adapters.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamAdapter;
import com.mainstreetcode.teammates.model.FeedItem;
import com.mainstreetcode.teammates.model.Team;
import com.squareup.picasso.Picasso;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

/**
 * Viewholder for a {@link Team}
 */
public class FeedItemViewHolder extends BaseViewHolder<TeamAdapter.TeamAdapterListener>
        implements View.OnClickListener {

    private FeedItem item;
    private ImageView teamLogo;
    private TextView description;
    private TextView itemType;

    public FeedItemViewHolder(View itemView) {
        super(itemView);
        teamLogo = itemView.findViewById(R.id.thumbnail);
        itemType = itemView.findViewById(R.id.message);
        description = itemView.findViewById(R.id.model);
        itemView.setOnClickListener(this);
    }

    public void bind(FeedItem item) {

        this.item = item;
        description.setText(item.getMessage());
        itemType.setText(item.getType());

        if (item.getImageUrl()!=null) {
            Picasso.with(itemView.getContext())
                    .load(item.getImageUrl())
                    .fit()
                    .centerInside()
                    .into(teamLogo);
        }
    }

    @Override
    public void onClick(View view) {
        //adapterListener.onTeamClicked(item);
    }
}
