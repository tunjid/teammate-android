package com.mainstreetcode.teammate.adapters.viewholders;

import androidx.core.view.ViewCompat;
import android.view.View;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.FeedAdapter;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Media;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.notifications.FeedItem;

import static androidx.core.view.ViewCompat.setTransitionName;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.getTransitionName;

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
        else if (model instanceof Event || model instanceof JoinRequest) {
            ViewCompat.setTransitionName(itemView, getTransitionName(item, R.id.fragment_header_background));
            ViewCompat.setTransitionName(thumbnail, getTransitionName(item, R.id.fragment_header_thumbnail));
        }
    }

    @Override
    public void onClick(View view) {
        adapterListener.onFeedItemClicked(item);
    }
}
