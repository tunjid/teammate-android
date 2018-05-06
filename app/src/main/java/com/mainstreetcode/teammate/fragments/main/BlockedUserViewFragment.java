package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.BlockedUserViewAdapter;
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment;
import com.mainstreetcode.teammate.model.BlockedUser;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.mainstreetcode.teammate.viewmodel.gofers.BlockedUserGofer;
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer;

public class BlockedUserViewFragment extends HeaderedFragment<BlockedUser> {

    public static final String ARG_BLOCKED_USER = "blockedUser";

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
        setHasOptionsMenu(true);
        blockedUser = getArguments().getParcelable(ARG_BLOCKED_USER);
        gofer = blockedUserViewModel.gofer(blockedUser);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_headered, container, false);

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.model_list))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new BlockedUserViewAdapter(gofer.getItems()))
                .withLinearLayoutManager()
                .build();

        scrollManager.getRecyclerView().requestFocus();

        return rootView;
    }

    @Override
    public void togglePersistentUi() {
        super.togglePersistentUi();
        setFabClickListener(this);
        setFabIcon(R.drawable.ic_unlock_white);
        setToolbarTitle(getString(R.string.blocked_user));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab)
            disposables.add(gofer.delete().subscribe(this::onUserUnblocked, defaultErrorHandler));
    }

    @Override
    public boolean[] insetState() {return VERTICAL;}

    @Override
    public boolean showsFab() {return gofer.hasPrivilegedRole();}

    @Override
    public void onImageClick() {
        showSnackbar(getString(R.string.no_permission));
    }

    @Override
    protected BlockedUser getHeaderedModel() {return blockedUser;}

    @Override
    protected Gofer<BlockedUser> gofer() {
        return gofer;
    }

    @Override
    protected void onModelUpdated(DiffUtil.DiffResult result) {}

    private void onUserUnblocked() {
        showSnackbar(getString(R.string.unblocked_user, blockedUser.getUser().getFirstName()));
        requireActivity().onBackPressed();
    }
}
