package com.mainstreetcode.teammates.fragments.main;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.EventEditAdapter;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.fragments.ImageWorkerFragment;
import com.mainstreetcode.teammates.model.Event;

/**
 * Edits a Team member
 */

public class EventEditFragment extends MainActivityFragment
        implements
        View.OnClickListener,
        ImageWorkerFragment.CropListener,
        ImageWorkerFragment.ImagePickerListener {

    private static final String ARG_EVENT = "event";

    private Event event;

    private RecyclerView recyclerView;

    public static EventEditFragment newInstance(Event event) {
        EventEditFragment fragment = new EventEditFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_EVENT, event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getStableTag() {
        String superResult = super.getStableTag();
        Event tempEvent = getArguments().getParcelable(ARG_EVENT);

        return (tempEvent != null)
                ? superResult + "-" + tempEvent.hashCode()
                : superResult;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        event = getArguments().getParcelable(ARG_EVENT);

        getChildFragmentManager().beginTransaction()
                .add(ImageWorkerFragment.newInstance(), ImageWorkerFragment.TAG)
                .commit();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_edit, container, false);
        recyclerView = rootView.findViewById(R.id.user_edit);


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new EventEditAdapter(event, true, this));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (Math.abs(dy) < 3) return;
                toggleFab(dy < 0);
            }
        });

        recyclerView.requestFocus();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        if (userViewModel.isTeamAdmin(event) && !userViewModel.getCurrentUser().equals(user)) {
//            inflater.inflate(R.menu.fragment_user_edit, menu);
//        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton fab = getFab();
        fab.setOnClickListener(this);
        fab.setImageResource(R.drawable.ic_check_white_24dp);
        toggleFab(true);
        setToolbarTitle(getString(R.string.edit_user));

//        disposables.add(roleViewModel.getRoleValues().subscribe(currentRoles -> {
//            roles.clear();
//            roles.addAll(currentRoles);
//        }));
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        ImageWorkerFragment fragment = (ImageWorkerFragment) childFragment;
        if (childFragment != null) fragment.setCropListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
        toggleFab(false);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
//                String role = user.getRoleName();
//
//                if (TextUtils.isEmpty(role)) {
//                    showSnackbar("Please select a role");
//                    return;
//                }
//
//                disposables.add(
//                        teamViewModel.updateTeamUser(event, user).subscribe(updatedUser -> {
//                            user.update(updatedUser);
//                            showSnackbar(getString(R.string.updated_user, user.getFirstName()));
//                            recyclerView.getAdapter().notifyDataSetChanged();
//                        }, defaultErrorHandler)
//                );
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_kick:
//                final String firstName = user.getFirstName();
//                final String prompt = getString(R.string.confirm_user_drop, firstName);
//
//                Snackbar.make(recyclerView, prompt, Snackbar.LENGTH_INDEFINITE)
//                        .setAction(R.string.yes, view ->
//                                disposables.add(teamViewModel.dropUser(event, user).subscribe(dropped -> {
//                                    showSnackbar(getString(R.string.dropped_user, firstName));
//                                    getActivity().onBackPressed();
//                                }, defaultErrorHandler)))
//                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onImageCropped(Uri uri) {
//        user.get(User.IMAGE_POSITION).setValue(uri.getPath());
//        recyclerView.getAdapter().notifyItemChanged(User.IMAGE_POSITION);
    }

    @Override
    public void onImageClick() {
        ImageWorkerFragment imageWorkerFragment = (ImageWorkerFragment) getChildFragmentManager()
                .findFragmentByTag(ImageWorkerFragment.TAG);

        if (imageWorkerFragment != null) imageWorkerFragment.requestCrop();
    }
}
