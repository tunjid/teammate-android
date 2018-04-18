package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.GuestAdapter;
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment;
import com.mainstreetcode.teammate.model.Guest;
import com.mainstreetcode.teammate.model.HeaderedModel;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.ScrollManager;

public class GuestViewFragment extends HeaderedFragment {

    private static final String ARG_GUEST = "guest";

    private Guest guest;

    public static GuestViewFragment newInstance(Guest guest) {
        GuestViewFragment fragment = new GuestViewFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_GUEST, guest);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        String superResult = super.getStableTag();
        Guest tempGuest = getArguments().getParcelable(ARG_GUEST);

        return (tempGuest != null)
                ? superResult + "-" + tempGuest.hashCode()
                : superResult;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        guest = getArguments().getParcelable(ARG_GUEST);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_headered, container, false);

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.model_list))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new GuestAdapter(guest, this))
                .withLinearLayoutManager()
                .build();

        scrollManager.getRecyclerView().requestFocus();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        disposables.add(userViewModel.getMe().subscribe(ignored -> {
            viewHolder.bind(getHeaderedModel());
            scrollManager.notifyDataSetChanged();
        }, defaultErrorHandler));
        disposables.add(localRoleViewModel.getRoleInTeam(userViewModel.getCurrentUser(), guest.getEvent().getTeam())
                .subscribe(requireActivity()::invalidateOptionsMenu, ErrorHandler.EMPTY));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.action_block);

        User current = userViewModel.getCurrentUser();
        boolean canBlockUser = localRoleViewModel.hasPrivilegedRole() && !current.equals(guest.getUser());

        item.setVisible(canBlockUser);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_guest_view, menu);
    }

    @Override
    public void togglePersistentUi() {
        super.togglePersistentUi();
        setToolbarTitle(getString(R.string.event_guest));
    }

    @Override
    public boolean[] insetState() {return VERTICAL;}

    @Override
    public boolean showsFab() {return false;}

    @Override
    protected HeaderedModel getHeaderedModel() {return guest;}

    @Override
    public void onImageClick() {
        showSnackbar(getString(R.string.no_permission));
    }
}
