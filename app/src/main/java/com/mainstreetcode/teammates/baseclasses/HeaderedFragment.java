package com.mainstreetcode.teammates.baseclasses;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
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

    private int lastOffset;
    private AppBarLayout appBarLayout;
    protected HeaderedImageViewHolder viewHolder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageWorkerFragment.attach(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        HeaderedModel model = getHeaderedModel();

        viewHolder = new HeaderedImageViewHolder(view, this);
        viewHolder.bind(model);

        setTransitionName(view, getTransitionName(model, R.id.fragment_header_background));
        setTransitionName(viewHolder.getPicture(), getTransitionName(model, R.id.fragment_header_thumbnail));

        Toolbar headerToolbar = view.findViewById(R.id.header_toolbar);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) headerToolbar.getLayoutParams();
        params.height += TeammatesBaseActivity.insetHeight;

        appBarLayout = view.findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            if (!showsFab() || getActivity() == null) return;

            int dy = lastOffset - verticalOffset;
            if (Math.abs(dy) < 3) return;

            toggleFab(dy < 0);
            lastOffset = verticalOffset;
        });
    }

    @Override
    public void onImageClick() {
        if (showsFab()) ImageWorkerFragment.requestCrop(this);
    }

    @Override
    public void onImageCropped(Uri uri) {
        Item item = getHeaderedModel().getHeaderItem();
        item.setValue(uri.getPath());
        viewHolder.bind(getHeaderedModel());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewHolder = null;
        appBarLayout = null;
    }

    @Override
    protected void onKeyBoardChanged(boolean appeared) {
        super.onKeyBoardChanged(appeared);
        if (appBarLayout != null && appeared) appBarLayout.setExpanded(false);
    }

    protected abstract HeaderedModel getHeaderedModel();
}
