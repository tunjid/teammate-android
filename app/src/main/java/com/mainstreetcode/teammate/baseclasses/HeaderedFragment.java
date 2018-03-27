package com.mainstreetcode.teammate.baseclasses;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.HeaderedImageViewHolder;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.HeaderedModel;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.util.ErrorHandler;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;

import static android.support.v4.view.ViewCompat.setTransitionName;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.getLayoutParams;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.getTransitionName;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public abstract class HeaderedFragment extends MainActivityFragment
        implements
        ImageWorkerFragment.CropListener,
        ImageWorkerFragment.ImagePickerListener {

    private static final int FAB_DELAY = 400;

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
        setTransitionName(viewHolder.getThumbnail(), getTransitionName(model, R.id.fragment_header_thumbnail));

        Toolbar headerToolbar = view.findViewById(R.id.header_toolbar);
        getLayoutParams(headerToolbar).height += TeammatesBaseActivity.topInset;

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
        if (!appeared) return;
        if (appBarLayout != null) appBarLayout.setExpanded(false);
        if (showsFab()) disposables.add(Completable.timer(FAB_DELAY, TimeUnit.MILLISECONDS)
                .observeOn(mainThread())
                .subscribe(() -> toggleFab(true), ErrorHandler.EMPTY));
    }

    protected abstract HeaderedModel getHeaderedModel();
}
