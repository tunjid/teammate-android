package com.mainstreetcode.teammate.adapters.viewholders;

import android.view.View;

import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Item;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

/**
 * Viewholder for {@link Item items}
 */
public class BaseItemViewHolder<T extends ImageWorkerFragment.ImagePickerListener> extends BaseViewHolder<T> {
    Item item;

    public BaseItemViewHolder(View itemView) {
        super(itemView);
    }

    public void bind(Item item) {
        this.item = item;
    }
}
