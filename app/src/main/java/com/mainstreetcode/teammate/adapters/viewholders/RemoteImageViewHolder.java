package com.mainstreetcode.teammate.adapters.viewholders;

import android.view.View;

import com.mainstreetcode.teammate.adapters.RemoteImageAdapter;
import com.mainstreetcode.teammate.model.RemoteImage;

public class RemoteImageViewHolder<T extends RemoteImage>
        extends ModelCardViewHolder<T, RemoteImageAdapter.AdapterListener<T>> {

    public RemoteImageViewHolder(View itemView, RemoteImageAdapter.AdapterListener<T> adapterListener) {
        super(itemView, adapterListener);

        title.setVisibility(View.GONE);
        subtitle.setVisibility(View.GONE);
        itemView.setOnClickListener(v -> adapterListener.onImageClicked(model));
    }
}
