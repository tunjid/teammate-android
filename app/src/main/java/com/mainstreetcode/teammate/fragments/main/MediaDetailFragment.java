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

package com.mainstreetcode.teammate.fragments.main;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.MediaAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.ImageMediaViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.MediaViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.VideoMediaViewHolder;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Media;
import com.tunjid.androidbootstrap.view.util.InsetFlags;

import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.isDisplayingSystemUI;


public class MediaDetailFragment extends MainActivityFragment
        implements MediaAdapter.MediaAdapterListener {

    static final String ARG_MEDIA = "media";

    private Media media;
    private MediaViewHolder mediaViewHolder;
    private AtomicBoolean systemUiStatus;

    public static MediaDetailFragment newInstance(Media media) {
        MediaDetailFragment fragment = new MediaDetailFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_MEDIA, media);
        fragment.setArguments(args);
        fragment.setEnterExitTransitions();

        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        return super.getStableTag() + "-" + getArguments().getParcelable(ARG_MEDIA);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        media = getArguments().getParcelable(ARG_MEDIA);
        systemUiStatus = new AtomicBoolean();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        boolean isImage = media.isImage();
        int resource = isImage ? R.layout.viewholder_image : R.layout.viewholder_video;

        return inflater.inflate(resource, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        disposables.add(mediaViewModel.getMedia(media).subscribe(this::checkMediaFlagged, defaultErrorHandler));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != R.id.action_flag_media) return super.onOptionsItemSelected(item);

        Activity activity = getActivity();
        if (activity == null) return true;

        new AlertDialog.Builder(activity)
                .setTitle(R.string.flag_media)
                .setMessage(R.string.flag_media_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> disposables.add(mediaViewModel.flagMedia(media).subscribe(this::checkMediaFlagged, defaultErrorHandler)))
                .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                .show();

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        View root = getView();
        if (root != null) bindViewHolder(root, media, media.isImage());
    }

    @Override
    public void onPause() {
        if (mediaViewHolder != null) mediaViewHolder.unBind();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        mediaViewHolder = null;
        super.onDestroyView();
    }

    @Override
    protected int getToolbarMenu() { return R.menu.fragment_media_detail; }

    @Override
    protected int getNavBarColor() { return Color.TRANSPARENT; }

    @Override
    public InsetFlags insetFlags() { return NONE; }

    @Override
    public boolean showsFab() { return false; }

    @Override
    public boolean showsBottomNav() { return false; }

    @Override
    public boolean showsToolBar() { return true; }

    @Override
    protected boolean showsSystemUI() { return systemUiStatus.get(); }

    @Override
    protected boolean hasLightNavBar() { return false; }

    @Override
    public void onMediaClicked(Media item) {
        Activity activity = getActivity();
        if (activity == null) return;

        systemUiStatus.set(isDisplayingSystemUI(activity.getWindow().getDecorView()));
        togglePersistentUi();
    }

    @Override
    public boolean onMediaLongClicked(Media media) { return false; }

    @Override
    public boolean isSelected(Media media) { return false; }

    @Override
    public boolean isFullScreen() { return true; }

    @Override
    public void onFillLoaded() { togglePersistentUi(); }

    private void checkMediaFlagged(Media media) {
        if (!media.isFlagged()) return;
        showSnackbar(getString(R.string.media_flagged));

        Activity activity = getActivity();
        if (activity != null) activity.onBackPressed();
    }

    private void bindViewHolder(View root, Media media, boolean isImage) {
        mediaViewHolder = isImage
                ? new ImageMediaViewHolder(root, this)
                : new VideoMediaViewHolder(root, this);

        mediaViewHolder.fullBind(media);
        onMediaClicked(media);
    }
}
