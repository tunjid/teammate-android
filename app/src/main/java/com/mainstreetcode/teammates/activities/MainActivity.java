package com.mainstreetcode.teammates.activities;

import android.app.Activity;
import android.arch.core.util.Function;
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
import com.mainstreetcode.teammates.fragments.main.FeedFragment;
import com.mainstreetcode.teammates.fragments.main.MediaFragment;
import com.mainstreetcode.teammates.fragments.main.SettingsFragment;
import com.mainstreetcode.teammates.fragments.main.TeamDetailFragment;
import com.mainstreetcode.teammates.fragments.main.TeamsFragment;
import com.mainstreetcode.teammates.model.Chat;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.notifications.TeammatesInstanceIdService;
import com.mainstreetcode.teammates.persistence.entity.JoinRequestEntity;
import com.mainstreetcode.teammates.util.Supplier;
import com.mainstreetcode.teammates.viewmodel.UserViewModel;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.core.view.ViewHider;

import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;
import static android.support.design.widget.BottomSheetBehavior.STATE_HIDDEN;
import static android.view.View.GONE;
import static com.tunjid.androidbootstrap.core.view.ViewHider.BOTTOM;

public class MainActivity extends TeammatesBaseActivity
        implements BottomSheetController {

    public static final String FEED_DEEP_LINK = "feed-deep-link";

    @Nullable
    private ViewHider bottombarHider;

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

            if (t.contains(FeedFragment.class.getSimpleName())) id = R.id.action_home;
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
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        View bottomBar = findViewById(R.id.bottom_navigation);
        bottombarHider = ViewHider.of(bottomBar).setDirection(BOTTOM)
                .addStartRunnable(() -> {
                    TeammatesBaseFragment view = (TeammatesBaseFragment) getCurrentFragment();
                    if (view != null && !view.showsBottomNav()) bottomBar.setVisibility(GONE);
                })
                .addEndRunnable(() -> {
                    TeammatesBaseFragment view = (TeammatesBaseFragment) getCurrentFragment();
                    if (view != null && view.showsBottomNav()) initTransition();
                })
                .build();
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
                showFragment(FeedFragment.newInstance());
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
        if (bottomSheetBehavior.getState() != STATE_HIDDEN) toggleBottomSheet(false);
        else super.onBackPressed();
    }

    @Override
    public void toggleBottombar(boolean show) {
        if (bottombarHider == null) return;
        if (show) bottombarHider.show();
        else bottombarHider.hide();
    }

    @Override
    public void toggleBottomSheet(boolean show) {
        bottomSheetBehavior.setState(show ? STATE_EXPANDED : STATE_HIDDEN);
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

        if (model != null) route = route(
                () -> route(model, Chat.class, Chat::getTeam, ChatFragment::newInstance),
                () -> route(model, Event.class, event -> event, EventEditFragment::newInstance),
                () -> route(model, JoinRequest.class, JoinRequestEntity::getTeam, TeamDetailFragment::newInstance)
        );

        else if (savedInstanceState == null) route = FeedFragment.newInstance();

        showFragment(route != null ? route : FeedFragment.newInstance());
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

    @Nullable
    @SuppressWarnings("unchecked")
    private <T, S> BaseFragment route(Model model, Class<T> modelClass, Function<T, S> function, Function<S, BaseFragment> fragmentFunction) {
        if (!model.getClass().equals(modelClass)) return null;
        return fragmentFunction.apply(function.apply((T) model));
    }

    @Nullable
    @SafeVarargs
    private final BaseFragment route(Supplier<BaseFragment>... suppliers) {
        for (Supplier<BaseFragment> supplier : suppliers) {
            BaseFragment fragment = supplier.get();
            if (fragment != null) return fragment;
        }
        return null;
    }

}
