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

package com.mainstreetcode.teammate.util.nav;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.RemoteImageAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.RemoteImageViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.TeamViewHolder;
import com.mainstreetcode.teammate.fragments.main.TeamMembersFragment;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.mainstreetcode.teammate.viewmodel.TeamViewModel;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import io.reactivex.disposables.Disposable;

import static android.widget.LinearLayout.HORIZONTAL;

public class NavDialogFragment extends BottomSheetDialogFragment {

    private TeamViewModel teamViewModel;

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

    @Override
    @SuppressWarnings("ConstantConditions")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bottom_nav, container, false);
        View itemView = root.findViewById(R.id.item_container);
        TeamViewHolder teamViewHolder = new TeamViewHolder(itemView, this::viewTeam);
        NavigationView navigationView = root.findViewById(R.id.bottom_nav_view);

        List<Team> list = new ArrayList<>();

        Disposable disposable = teamViewModel.nonDefaultTeams(list)
                .subscribe(ScrollManager.<RemoteImageViewHolder<Team>>with(root.findViewById(R.id.horizontal_list))
                        .withAdapter(new RemoteImageAdapter<>(list, team -> {
                            teamViewModel.updateDefaultTeam(team);
                            viewTeam(team);
                        }))
                        .withCustomLayoutManager(new LinearLayoutManager(root.getContext(), HORIZONTAL, false))
                        .build()::onDiff, ErrorHandler.EMPTY);

        itemView.setElevation(0);
        teamViewHolder.bind(teamViewModel.getDefaultTeam());
        navigationView.setNavigationItemSelectedListener(this::onOptionsItemSelected);
        root.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override public void onViewAttachedToWindow(View v) {}

            @Override public void onViewDetachedFromWindow(View v) { disposable.dispose(); }
        });
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

    private void viewTeam(Team team) {
        dismiss();
        ((BaseActivity) requireActivity()).showFragment(TeamMembersFragment.newInstance(team));
    }
}
