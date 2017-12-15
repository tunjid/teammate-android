package com.mainstreetcode.teammates.baseclasses;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.HeaderedImageViewHolder;
import com.mainstreetcode.teammates.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammates.model.HeaderedModel;
import com.mainstreetcode.teammates.model.Item;

import static android.support.v4.view.ViewCompat.setTransitionName;
import static com.mainstreetcode.teammates.util.ViewHolderUtil.getTransitionName;

public abstract class HeaderedFragment extends MainActivityFragment
        implements
        ImageWorkerFragment.CropListener,
        ImageWorkerFragment.ImagePickerListener {

    protected HeaderedImageViewHolder viewHolder;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        HeaderedModel model = getHeaderedModel();
        Item headerItem = model.getHeaderItem();

        viewHolder = new HeaderedImageViewHolder(view, this);
        viewHolder.bind(headerItem);

        setTransitionName(view, getTransitionName(model, R.id.fragment_header_background));
        setTransitionName(viewHolder.getPicture(), getTransitionName(model, R.id.fragment_header_thumbnail));

        Toolbar headerToolbar = view.findViewById(R.id.header_toolbar);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) headerToolbar.getLayoutParams();
        params.height += TeammatesBaseActivity.insetHeight;
    }

    @Override
    public void onImageCropped(Uri uri) {
        Item item = getHeaderedModel().getHeaderItem();
        item.setValue(uri.getPath());
        viewHolder.bind(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewHolder = null;
    }

    protected abstract HeaderedModel getHeaderedModel();
}
