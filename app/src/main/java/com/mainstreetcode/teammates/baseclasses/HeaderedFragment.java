package com.mainstreetcode.teammates.baseclasses;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.ImageViewHolder;
import com.mainstreetcode.teammates.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammates.model.HeaderedModel;
import com.mainstreetcode.teammates.model.Item;

public abstract class HeaderedFragment extends MainActivityFragment
        implements
        ImageWorkerFragment.CropListener,
        ImageWorkerFragment.ImagePickerListener{

    protected ImageViewHolder imageViewHolder;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageViewHolder = new ImageViewHolder(view, this);
        imageViewHolder.bind(getHeaderedModel().getHeaderItem());

        Toolbar headerToolbar = view.findViewById(R.id.header_toolbar);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) headerToolbar.getLayoutParams();
        params.height += TeammatesBaseActivity.insetHeight;
    }

    @Override
    public void onImageCropped(Uri uri) {
        Item item = getHeaderedModel().getHeaderItem();
        item.setValue(uri.getPath());
        imageViewHolder.bind(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        imageViewHolder = null;
    }

    protected abstract HeaderedModel getHeaderedModel();
}
