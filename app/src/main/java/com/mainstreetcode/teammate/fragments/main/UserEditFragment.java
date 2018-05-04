package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.UserAdapter;
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer;
import com.mainstreetcode.teammate.viewmodel.gofers.UserGofer;

/**
 * Edits a Team member
 */

public class UserEditFragment extends HeaderedFragment<User>
        implements
        ImageWorkerFragment.ImagePickerListener {

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
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new UserAdapter(gofer.getItems(), this))
                .withLinearLayoutManager()
                .build();

        scrollManager.getRecyclerView().requestFocus();

        return rootView;
    }

    @Override
    public void togglePersistentUi() {
        super.togglePersistentUi();
        setFabClickListener(this);
        setFabIcon(R.drawable.ic_check_white_24dp);
        setToolbarTitle(getString(R.string.edit_user));
    }

    @Override
    public boolean[] insetState() {return VERTICAL;}

    @Override
    public boolean showsFab() {return true;}

    @Override
    protected User getHeaderedModel() {return user;}

    @Override
    protected Gofer<User> gofer() { return gofer; }

    @Override
    protected void onModelUpdated(DiffUtil.DiffResult result) {
        scrollManager.onDiff(result);
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
