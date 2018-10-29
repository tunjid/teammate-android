package com.mainstreetcode.teammate.util.nav;

import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.TeamViewHolder;
import com.mainstreetcode.teammate.fragments.headless.TeamPickerFragment;
import com.mainstreetcode.teammate.viewmodel.TeamViewModel;

public class NavDialogFragment extends BottomSheetDialogFragment {

    TeamViewModel teamViewModel;

    public static NavDialogFragment newInstance() {
        NavDialogFragment fragment = new NavDialogFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onAttach(Context context) {
        super.onAttach(context);
        teamViewModel = ViewModelProviders.of(requireActivity()).get(TeamViewModel.class);
    }

    @Nullable
    @Override
    @SuppressWarnings("ConstantConditions")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bottom_nav, container, false);
        View itemView = root.findViewById(R.id.item_container);
        TeamViewHolder teamViewHolder = new TeamViewHolder(itemView, item -> switchTeam());
        NavigationView navigationView = root.findViewById(R.id.bottom_nav_view);

        itemView.setElevation(0);
        teamViewHolder.bind(teamViewModel.getDefaultTeam());
        navigationView.setNavigationItemSelectedListener(this::onOptionsItemSelected);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        View root = getView();
        if (root == null || !(root.getParent() instanceof View)) return;
        root = (View) root.getParent();
        root.setBackgroundResource(R.drawable.bg_nav_dialog);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        dismiss();
        return requireActivity().onOptionsItemSelected(item);
    }

    private void switchTeam() {
        dismiss();
        TeamPickerFragment.change(requireActivity(), R.id.request_default_team_pick);
    }
}
