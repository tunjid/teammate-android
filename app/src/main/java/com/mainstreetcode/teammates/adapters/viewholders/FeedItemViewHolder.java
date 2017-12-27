package com.mainstreetcode.teammates.adapters.viewholders;

import android.view.View;

import com.mainstreetcode.teammates.adapters.FeedAdapter;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.notifications.FeedItem;
import com.mainstreetcode.teammates.notifications.Notifiable;

/**
 * Viewholder for a {@link Team}
 */
public class FeedItemViewHolder<H extends Model<H> & Notifiable<H>> extends ModelCardViewHolder<H, FeedAdapter.FeedItemAdapterListener>
        implements View.OnClickListener {

    private FeedItem<H> item;

    public FeedItemViewHolder(View itemView, FeedAdapter.FeedItemAdapterListener listener) {
        super(itemView, listener);
        itemView.setOnClickListener(this);
    }

    public void bind(FeedItem<H> item) {
        this.item = item;
        bind(item.getModel());

        title.setText(item.getTitle());
        subtitle.setText(item.getBody());
    }

    @Override
    public void bind(H model) {
        super.bind(model);
    }

    @Override
    public void onClick(View view) {
        adapterListener.onFeedItemClicked(item);
    }
}
