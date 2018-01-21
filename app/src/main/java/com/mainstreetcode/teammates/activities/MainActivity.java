package com.mainstreetcode.teammates.activities;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.baseclasses.BottomSheetController;
import com.mainstreetcode.teammates.baseclasses.TeammatesBaseActivity;
import com.mainstreetcode.teammates.baseclasses.TeammatesBaseFragment;
import com.mainstreetcode.teammates.fragments.headless.TeamPickerFragment;
import com.mainstreetcode.teammates.fragments.main.ChatFragment;
import com.mainstreetcode.teammates.fragments.main.EventEditFragment;
import com.mainstreetcode.teammates.fragments.main.EventsFragment;
import com.mainstreetcode.teammates.fragments.main.HomeFragment;
import com.mainstreetcode.teammates.fragments.main.MediaFragment;
import com.mainstreetcode.teammates.fragments.main.SettingsFragment;
import com.mainstreetcode.teammates.fragments.main.TeamsFragment;
import com.mainstreetcode.teammates.model.Chat;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.notifications.TeammatesInstanceIdService;
import com.mainstreetcode.teammates.viewmodel.UserViewModel;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;

public class MainActivity extends TeammatesBaseActivity
        implements BottomSheetController {

    public static final String FEED_DEEP_LINK = "feed-deep-link";

    private BottomNavigationView bottomNavigationView;
    private BottomSheetBehavior bottomSheetBehavior;
    private View bottomSheetContainer;

    final FragmentManager.FragmentLifecycleCallbacks lifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v, Bundle savedInstanceState) {
            if (!isInMainFragmentContainer(v)) return;

            Menu menu = bottomNavigationView.getMenu();
            String t = f.getTag();

            if (t == null) return;
            int id = 0;

            if (t.contains(HomeFragment.class.getSimpleName())) id = R.id.action_home;
            else if (t.contains(EventsFragment.class.getSimpleName())) id = R.id.action_events;
            else if (t.contains(ChatFragment.class.getSimpleName())) id = R.id.action_messages;
            else if (t.contains(MediaFragment.class.getSimpleName())) id = R.id.action_media;
            else if (t.contains(TeamsFragment.class.getSimpleName())) id = R.id.action_team;

            if (id == 0) return;
            MenuItem menuItem = menu.findItem(id);

            if (menuItem != null) menuItem.setChecked(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(lifecycleCallbacks, false);

        UserViewModel userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);

        if (!userViewModel.isSignedIn()) {
            startRegistrationActivity(this);
            return;
        }

        TeammatesInstanceIdService.updateFcmToken();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomSheetContainer = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer);

        bottomNavigationView.setOnNavigationItemSelectedListener(this::onOptionsItemSelected);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState != STATE_COLLAPSED) return;
                restoreHiddenViewState();
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        route(savedInstanceState, getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        route(null, intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_home:
                showFragment(HomeFragment.newInstance());
                return true;
            case R.id.action_events:
                TeamPickerFragment.pick(this, R.id.request_event_team_pick);
                return true;
            case R.id.action_messages:
                TeamPickerFragment.pick(this, R.id.request_chat_team_pick);
                return true;
            case R.id.action_media:
                TeamPickerFragment.pick(this, R.id.request_media_team_pick);
                return true;
            case R.id.action_team:
                showFragment(TeamsFragment.newInstance());
                return true;
            case R.id.action_settings:
                showFragment(SettingsFragment.newInstance());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() != STATE_COLLAPSED) toggleBottomSheet(false);
        else super.onBackPressed();
    }

    @Override
    public void toggleBottomSheet(boolean show) {
        bottomSheetBehavior.setState(show ? STATE_EXPANDED : STATE_COLLAPSED);
        if (!show) restoreHiddenViewState();
        else bottomSheetContainer.setPadding(0, TeammatesBaseActivity.insetHeight, 0, 0);
    }

    @Override
    public boolean showFragment(BaseFragment fragment) {
        toggleBottomSheet(false);
        return super.showFragment(fragment);
    }

    private void route(@Nullable Bundle savedInstanceState, @NonNull Intent intent) {
        Model model = intent.getParcelableExtra(FEED_DEEP_LINK);
        BaseFragment route = null;

        if (model != null) {
            if (model instanceof Event) route = EventEditFragment.newInstance((Event) model);
            if (model instanceof Chat) route = ChatFragment.newInstance(((Chat) model).getTeam());
        }
        else if (savedInstanceState == null) route = HomeFragment.newInstance();

        if (route != null) showFragment(route);
    }

    private void restoreHiddenViewState() {
        TeammatesBaseFragment fragment = (TeammatesBaseFragment) getCurrentFragment();
        if (fragment == null) return;

        toggleFab(fragment.showsFab());
        toggleToolbar(fragment.showsToolBar());
        toggleBottombar(fragment.showsBottomNav());
    }

    public static void startRegistrationActivity(Activity activity) {
        Intent main = new Intent(activity, RegistrationActivity.class);
        activity.startActivity(main);
        activity.finish();
    }
}
