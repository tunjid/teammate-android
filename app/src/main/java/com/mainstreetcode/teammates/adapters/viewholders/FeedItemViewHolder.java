package com.mainstreetcode.teammates.adapters.viewholders;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.FeedAdapter;
import com.mainstreetcode.teammates.notifications.FeedItem;
import com.mainstreetcode.teammates.model.Team;
import com.squareup.picasso.Picasso;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

/**
 * Viewholder for a {@link Team}
 */
public class FeedItemViewHolder extends BaseViewHolder<FeedAdapter.FeedItemAdapterListener>
        implements View.OnClickListener {

    private FeedItem item;
    private ImageView teamLogo;
    private TextView description;
    private TextView itemType;

    public FeedItemViewHolder(View itemView, FeedAdapter.FeedItemAdapterListener listener) {
        super(itemView, listener);
        teamLogo = itemView.findViewById(R.id.thumbnail);
        itemType = itemView.findViewById(R.id.message);
        description = itemView.findViewById(R.id.model);
        itemView.setOnClickListener(this);
    }

    public void bind(FeedItem item) {

        this.item = item;
        description.setText(item.getBody());
        itemType.setText(item.getTitle());

        if (!TextUtils.isEmpty(item.getImageUrl())) {
            Picasso.with(itemView.getContext())
                    .load(item.getImageUrl())
                    .fit()
                    .centerInside()
                    .into(teamLogo);
        }
    }

    @Override
    public void onClick(View view) {
        adapterListener.onFeedItemClicked(item);
    }
}
