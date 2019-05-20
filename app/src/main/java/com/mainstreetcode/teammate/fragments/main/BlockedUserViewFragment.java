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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.BlockedUserViewAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder;
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment;
import com.mainstreetcode.teammate.model.BlockedUser;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.mainstreetcode.teammate.viewmodel.gofers.BlockedUserGofer;
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer;
import com.tunjid.androidbootstrap.view.util.InsetFlags;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.DiffUtil;

public class BlockedUserViewFragment extends HeaderedFragment<BlockedUser> {

    static final String ARG_BLOCKED_USER = "blockedUser";

    private BlockedUser blockedUser;
    private BlockedUserGofer gofer;

    public static BlockedUserViewFragment newInstance(BlockedUser guest) {
        BlockedUserViewFragment fragment = new BlockedUserViewFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_BLOCKED_USER, guest);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        return Gofer.tag(super.getStableTag(), getArguments().getParcelable(ARG_BLOCKED_USER));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blockedUser = getArguments().getParcelable(ARG_BLOCKED_USER);
        gofer = blockedUserViewModel.gofer(blockedUser);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_headered, container, false);

        scrollManager = ScrollManager.<InputViewHolder>with(rootView.findViewById(R.id.model_list))
                .withAdapter(new BlockedUserViewAdapter(gofer.getItems()))
                .addScrollListener((dx, dy) -> updateFabForScrollState(dy))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .build();

        scrollManager.getRecyclerView().requestFocus();

        return rootView;
    }

    @Override
    @StringRes
    protected int getFabStringResource() { return R.string.unblock_user; }

    @Override
    @DrawableRes
    protected int getFabIconResource() { return R.drawable.ic_unlock_white; }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab)
            disposables.add(gofer.delete().subscribe(this::onUserUnblocked, defaultErrorHandler));
    }

    @Override
    public boolean showsFab() {return gofer.hasPrivilegedRole();}

    @Override
    public void onImageClick() {
        showSnackbar(getString(R.string.no_permission));
    }

    @Override
    public InsetFlags insetFlags() {return NO_TOP;}

    @Override
    protected CharSequence getToolbarTitle() {
        return getString(R.string.blocked_user);
    }

    @Override
    protected BlockedUser getHeaderedModel() {return blockedUser;}

    @Override
    protected Gofer<BlockedUser> gofer() {
        return gofer;
    }

    @Override
    protected void onModelUpdated(DiffUtil.DiffResult result) {
        scrollManager.onDiff(result);
    }

    @Override
    protected void onPrepComplete() {
        requireActivity().invalidateOptionsMenu();
        super.onPrepComplete();
    }

    private void onUserUnblocked() {
        showSnackbar(getString(R.string.unblocked_user, blockedUser.getUser().getFirstName()));
        requireActivity().onBackPressed();
    }
}
