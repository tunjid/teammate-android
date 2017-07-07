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
import com.mainstreetcode.teammates.fragments.ImageWorkerFragment;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Item;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

/**
 * Adapter for {@link com.mainstreetcode.teammates.model.Event  }
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

public class EventEditAdapter extends BaseRecyclerViewAdapter<BaseItemViewHolder, ImageWorkerFragment.ImagePickerListener> {

    private static final int PADDING = 11;

    private final Event event;
    private final boolean isEditable;

    public EventEditAdapter(Event event, boolean isEditable, ImageWorkerFragment.ImagePickerListener listener) {
        super(listener);
        this.event = event;
        this.isEditable = isEditable;
    }

    @Override
    public BaseItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();

        @LayoutRes int layoutRes = viewType == Item.INPUT || viewType == Item.DATE
                ? R.layout.viewholder_simple_input
                : viewType == Item.IMAGE
                ? R.layout.viewholder_item_image
                : R.layout.view_holder_padding;

        View itemView = LayoutInflater.from(context).inflate(layoutRes, viewGroup, false);

        switch (viewType) {
            case Item.INPUT:
                return new InputViewHolder(itemView, isEditable);
            case Item.DATE:
                return new DateViewHolder(itemView);
            case Item.IMAGE:
                return new ImageViewHolder(itemView, adapterListener);
            default:
                return new BaseItemViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(BaseItemViewHolder baseEventViewHolder, int i) {
        if (i == event.size()) return;
        baseEventViewHolder.bind(event.get(i));
    }

    @Override
    public int getItemCount() {
        return event.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == event.size() ? PADDING : event.get(position).getItemType();
    }
}
