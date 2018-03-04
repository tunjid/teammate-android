package com.mainstreetcode.teammates.adapters.viewholders;

import android.view.View;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.FeedAdapter;
import com.mainstreetcode.teammates.model.Media;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.notifications.FeedItem;

import static android.support.v4.view.ViewCompat.setTransitionName;
import static com.mainstreetcode.teammates.util.ViewHolderUtil.getTransitionName;

/**
 * Viewholder for a {@link Team}
 */
public class FeedItemViewHolder<H extends Model<H>> extends ModelCardViewHolder<H, FeedAdapter.FeedItemAdapterListener>
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
        if (model instanceof Media) {
            setTransitionName(itemView, getTransitionName(model, R.id.fragment_media_background));
            setTransitionName(thumbnail, getTransitionName(model, R.id.fragment_media_thumbnail));
        }
    }

    @Override
    public void onClick(View view) {
        adapterListener.onFeedItemClicked(item);
    }

    @Override
    public boolean isThumbnail() {return true;}
}
