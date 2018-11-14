package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.DiffUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.UserEditAdapter;
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer;
import com.mainstreetcode.teammate.viewmodel.gofers.UserGofer;
import com.tunjid.androidbootstrap.view.util.InsetFlags;

/**
 * Edits a Team member
 */

public class UserEditFragment extends HeaderedFragment<User>
        implements
        UserEditAdapter.AdapterListener {

    private static final String ARG_USER = "user";

    private User user;
    private UserGofer gofer;

    public static UserEditFragment newInstance(User user) {
        UserEditFragment fragment = new UserEditFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        return Gofer.tag(super.getStableTag(), getArguments().getParcelable(ARG_USER));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = getArguments().getParcelable(ARG_USER);
        gofer = userViewModel.gofer(user);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_headered, container, false);

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.model_list))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), this::refresh)
                .withAdapter(new UserEditAdapter(gofer.getItems(), this))
                .addScrollListener((dx, dy) -> updateFabForScrollState(dy))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withRecycledViewPool(inputRecycledViewPool())
                .withLinearLayoutManager()
                .build();

        scrollManager.getRecyclerView().requestFocus();

        return rootView;
    }

    @Override
    public void togglePersistentUi() {
        updateFabIcon();
        setFabClickListener(this);
        setToolbarTitle(getString(canEdit() ? R.string.user_edit : R.string.user_info));
        super.togglePersistentUi();
    }

    @Override
    @StringRes
    protected int getFabStringResource() { return R.string.user_update; }

    @Override
    @DrawableRes
    protected int getFabIconResource() { return R.drawable.ic_check_white_24dp; }

    @Override
    public InsetFlags insetFlags() {return VERTICAL;}

    @Override
    public boolean showsFab() {return canEdit();}

    @Override
    protected User getHeaderedModel() {return user;}

    @Override
    protected Gofer<User> gofer() { return gofer; }

    @Override
    public boolean canEdit() { return userViewModel.getCurrentUser().equals(user); }

    @Override
    protected void onModelUpdated(DiffUtil.DiffResult result) {
        viewHolder.bind(getHeaderedModel());
        scrollManager.onDiff(result);
        toggleProgress(false);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                toggleProgress(true);
                disposables.add(gofer.save().subscribe(result -> {
                    showSnackbar(getString(R.string.updated_user, user.getFirstName()));
                    toggleProgress(false);
                    viewHolder.bind(getHeaderedModel());
                    scrollManager.onDiff(result);
                }, defaultErrorHandler));
                break;
        }
    }
}
