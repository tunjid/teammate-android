/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.baseclasses;


import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.appbar.AppBarLayout;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.HeaderedImageViewHolder;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.BlockedUser;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.HeaderedModel;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.ListableModel;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.model.enums.BlockReason;
import com.mainstreetcode.teammate.util.AppBarListener;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DiffUtil;
import io.reactivex.Flowable;

import static androidx.core.view.ViewCompat.setTransitionName;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.getLayoutParams;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.getTransitionName;
import static io.reactivex.Completable.timer;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public abstract class HeaderedFragment<T extends HeaderedModel<T> & ListableModel<T>> extends MainActivityFragment
        implements
        ImageWorkerFragment.CropListener,
        ImageWorkerFragment.ImagePickerListener {

    private static final int FAB_DELAY = 400;

    private boolean imageJustCropped;

    private AppBarLayout appBarLayout;
    protected HeaderedImageViewHolder viewHolder;

    protected abstract T getHeaderedModel();

    protected abstract Gofer<T> gofer();

    protected abstract void onModelUpdated(DiffUtil.DiffResult result);

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
        view.findViewById(R.id.header).setVisibility(canExpandAppBar() ? View.VISIBLE : View.GONE);

        AppBarListener.with()
                .appBarLayout(appBarLayout)
                .offsetDiffListener(this::onAppBarOffset)
                .create();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetch();
    }

    @Override
    public void onImageClick() {
        viewHolder.bind(getHeaderedModel());

        String errorMessage = gofer().getImageClickMessage(this);

        if (errorMessage == null) ImageWorkerFragment.requestCrop(this);
        else showSnackbar(errorMessage);
    }

    @Override
    public void onImageCropped(Uri uri) {
        Item item = getHeaderedModel().getHeaderItem();
        item.setValue(uri.getPath());
        viewHolder.bind(getHeaderedModel());
        imageJustCropped = true;
    }

    @Override
    public void onDestroyView() {
        gofer().clear();
        viewHolder.unBind();
        viewHolder = null;
        appBarLayout = null;
        super.onDestroyView();
    }

    protected void onPrepComplete() {
        togglePersistentUi();
    }

    protected boolean canExpandAppBar() {return true;}

    protected boolean cantGetModel() {
        boolean result = !ImageWorkerFragment.isPicking(this) && !imageJustCropped;
        imageJustCropped = false;
        return !result;
    }

    @Override
    protected void onKeyBoardChanged(boolean appeared) {
        super.onKeyBoardChanged(appeared);
        if (!appeared) return;
        if (appBarLayout != null) appBarLayout.setExpanded(false);
        if (showsFab()) disposables.add(timer(FAB_DELAY, MILLISECONDS)
                .observeOn(mainThread())
                .subscribe(this::togglePersistentUi, ErrorHandler.EMPTY));
    }

    protected final void fetch() { getData(false); }

    protected final void refresh() { getData(true); }

    private void getData(boolean refresh) {
        checkIfChanged();

        Flowable<DiffUtil.DiffResult> diffFlowable = gofer().get().doOnComplete(() -> {
            if (refresh) scrollManager.notifyDataSetChanged();
        });

        if (cantGetModel()) return;

        Runnable get = () -> disposables.add(diffFlowable.subscribe(this::onModelUpdated, defaultErrorHandler, this::checkIfChanged));

        if (refresh) get.run();
        else appBarLayout.postDelayed(() -> { if (getView() != null) get.run(); }, 800);
    }

    private void checkIfChanged() {
        disposables.add(gofer().watchForChange().subscribe(value -> onPrepComplete(), ErrorHandler.EMPTY));
    }

    private void onAppBarOffset(AppBarListener.OffsetProps offsetProps) {
        if (!offsetProps.appBarUnmeasured()) updateFabForScrollState(offsetProps.getDy());
    }

    protected void blockUser(User user, Team team) {
        List<BlockReason> reasons = Config.getBlockReasons();
        List<CharSequence> sequences = new ArrayList<>(reasons.size());

        for (BlockReason reason : reasons) sequences.add(reason.getName());

        new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.block_user)
                .setItems(sequences.toArray(new CharSequence[0]), (dialog, index) -> {
                    BlockReason reason = reasons.get(index);
                    BlockedUser request = BlockedUser.block(user, team, reason);

                    disposables.add(blockedUserViewModel.blockUser(request).subscribe(this::onUserBlocked, defaultErrorHandler));
                    toggleProgress(true);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void onUserBlocked(BlockedUser blockedUser) {
        showSnackbar(getString(R.string.user_blocked, blockedUser.getUser().getFirstName()));
        toggleProgress(false);
        Activity activity = getActivity();
        if (activity != null) activity.onBackPressed();
    }
}
