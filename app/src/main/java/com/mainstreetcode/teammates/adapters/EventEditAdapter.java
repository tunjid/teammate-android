package com.mainstreetcode.teammates.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.DateViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.ImageViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.TeamViewHolder;
import com.mainstreetcode.teammates.fragments.ImageWorkerFragment;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Item;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

/**
 * Adapter for {@link com.mainstreetcode.teammates.model.Event  }
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

public class EventEditAdapter extends BaseRecyclerViewAdapter<BaseViewHolder, EventEditAdapter.EditAdapterListener> {

    private static final int PADDING = 11;
    private static final int TEAM = 12;

    private final Event event;
    private final boolean isEditable;

    public EventEditAdapter(Event event, boolean isEditable, EditAdapterListener listener) {
        super(listener);
        this.event = event;
        this.isEditable = isEditable;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();

        @LayoutRes int layoutRes = viewType == Item.INPUT || viewType == Item.DATE
                ? R.layout.viewholder_simple_input
                : viewType == Item.IMAGE
                ? R.layout.viewholder_item_image
                : viewType == TEAM
                ? R.layout.viewholder_team
                : R.layout.view_holder_padding;

        View itemView = LayoutInflater.from(context).inflate(layoutRes, viewGroup, false);

        switch (viewType) {
            case Item.INPUT:
                return new InputViewHolder(itemView, isEditable);
            case Item.DATE:
                return new DateViewHolder(itemView);
            case Item.IMAGE:
                return new ImageViewHolder(itemView, adapterListener);
            case TEAM:
                return new TeamViewHolder(itemView, item -> adapterListener.selectTeam());
            default:
                return new BaseItemViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder viewHolder, int i) {
        int size = event.size();
        if (i > size) return;
        if (i == size) ((TeamViewHolder) viewHolder).bind(event.getTeam());
        else ((BaseItemViewHolder) viewHolder).bind(event.get(i));
    }

    @Override
    public int getItemCount() {
        return event.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        int size = event.size();
        return position > size ? PADDING : position == size ? TEAM : event.get(position).getItemType();
    }

    public interface EditAdapterListener extends
            TeamAdapter.TeamAdapterListener,
            ImageWorkerFragment.ImagePickerListener {

        void selectTeam();
    }
}
