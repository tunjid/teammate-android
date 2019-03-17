package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.GuestAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder;
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment;
import com.mainstreetcode.teammate.model.Guest;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer;
import com.mainstreetcode.teammate.viewmodel.gofers.GuestGofer;
import com.tunjid.androidbootstrap.view.util.InsetFlags;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

public class GuestViewFragment extends HeaderedFragment<Guest> {

    private static final String ARG_GUEST = "guest";

    private Guest guest;
    private GuestGofer gofer;

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
        return Gofer.tag(super.getStableTag(), getArguments().getParcelable(ARG_GUEST));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        guest = getArguments().getParcelable(ARG_GUEST);
        gofer = eventViewModel.gofer(guest);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_headered, container, false);

        scrollManager = ScrollManager.<InputViewHolder>with(rootView.findViewById(R.id.model_list))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), this::refresh)
                .withAdapter(new GuestAdapter(gofer.getItems(), this))
                .addScrollListener((dx, dy) -> updateFabForScrollState(dy))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withRecycledViewPool(inputRecycledViewPool())
                .withLinearLayoutManager()
                .build();

        scrollManager.getRecyclerView().requestFocus();

        return rootView;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.action_block);
        item.setVisible(gofer.canBlockUser());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_block) {
            blockUser(guest.getUser(), guest.getEvent().getTeam());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getToolbarMenu() { return R.menu.fragment_guest_view; }

    @Override
    protected CharSequence getToolbarTitle() {
        return getString(R.string.event_guest);
    }

    @Override
    public InsetFlags insetFlags() {return NO_TOP;}

    @Override
    public boolean showsFab() {return false;}

    @Override
    public void onImageClick() {
        showSnackbar(getString(R.string.no_permission));
    }

    @Override
    protected Guest getHeaderedModel() {return guest;}

    @Override
    protected Gofer<Guest> gofer() {
        return gofer;
    }

    @Override
    protected void onModelUpdated(DiffUtil.DiffResult result) {
        viewHolder.bind(getHeaderedModel());
        scrollManager.onDiff(result);
        toggleProgress(false);
    }

    @Override
    protected void onPrepComplete() {
        requireActivity().invalidateOptionsMenu();
        super.onPrepComplete();
    }
}
