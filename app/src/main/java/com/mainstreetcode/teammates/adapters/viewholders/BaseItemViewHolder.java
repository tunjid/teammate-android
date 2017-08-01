package com.mainstreetcode.teammates.adapters.viewholders;

import android.view.View;

import com.mainstreetcode.teammates.fragments.ImageWorkerFragment;
import com.mainstreetcode.teammates.model.Item;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

/**
 * Viewholder for {@link Item items}
 */
public class BaseItemViewHolder extends BaseViewHolder<ImageWorkerFragment.ImagePickerListener> {
    Item item;

    public BaseItemViewHolder(View itemView) {
        super(itemView);
    }

    BaseItemViewHolder(View itemView, ImageWorkerFragment.ImagePickerListener listener) {
        super(itemView, listener);
    }

    public void bind(Item item) {
        this.item = item;
    }
}
