package com.mainstreetcode.teammate.adapters.viewholders.input;

import android.view.View;

import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Item;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveViewHolder;


/**
 * Viewholder for {@link Item items}
 */
public class BaseItemViewHolder<T extends ImageWorkerFragment.ImagePickerListener> extends InteractiveViewHolder<T> {

    TextInputStyle textInputStyle;

    public BaseItemViewHolder(View itemView) {
        super(itemView);
    }

    public void bind(TextInputStyle textInputStyle) {
        this.textInputStyle = textInputStyle;
    }
}
