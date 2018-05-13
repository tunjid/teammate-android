package com.mainstreetcode.teammate.activities;

import android.app.Activity;
import android.arch.core.util.Function;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.baseclasses.BottomSheetController;
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseActivity;
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseFragment;
import com.mainstreetcode.teammate.fragments.headless.TeamPickerFragment;
import com.mainstreetcode.teammate.fragments.main.ChatFragment;
import com.mainstreetcode.teammate.fragments.main.EventEditFragment;
import com.mainstreetcode.teammate.fragments.main.EventSearchFragment;
import com.mainstreetcode.teammate.fragments.main.EventsFragment;
import com.mainstreetcode.teammate.fragments.main.FeedFragment;
import com.mainstreetcode.teammate.fragments.main.MediaFragment;
import com.mainstreetcode.teammate.fragments.main.MyEventsFragment;
import com.mainstreetcode.teammate.fragments.main.SettingsFragment;
import com.mainstreetcode.teammate.fragments.main.TeamMembersFragment;
import com.mainstreetcode.teammate.fragments.main.TeamsFragment;
import com.mainstreetcode.teammate.model.Chat;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.notifications.TeammatesInstanceIdService;
import com.mainstreetcode.teammate.persistence.entity.JoinRequestEntity;
import com.mainstreetcode.teammate.util.Supplier;
import com.mainstreetcode.teammate.viewmodel.UserViewModel;
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
    private Toolbar bottomSheetToolbar;
    private Toolbar altToolbar;

    final FragmentManager.FragmentLifecycleCallbacks lifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v, Bundle savedInstanceState) {
            if (isNotInMainFragmentContainer(v)) return;

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

        altToolbar = findViewById(R.id.alt_toolbar);
        bottomSheetToolbar = findViewById(R.id.bottom_toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));

        altToolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);
        bottomSheetToolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onOptionsItemSelected);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState != STATE_COLLAPSED) return;
                restoreHiddenViewState();
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });

        route(savedInstanceState, getIntent());
        App.prime();
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
                    if (view == null || !view.showsBottomNav()) return;
                    bottomBar.setVisibility(View.VISIBLE);
                    initTransition();
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
            case R.id.action_rsvp_list:
                showFragment(MyEventsFragment.newInstance());
                return true;
            case R.id.action_public_events:
                showFragment(EventSearchFragment.newInstance());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() != STATE_HIDDEN) hideBottomSheet();
        else super.onBackPressed();
    }

    @Override
    public void setAltToolbarMenu(@MenuRes int menu) {
        altToolbar.getMenu().clear();
        altToolbar.inflateMenu(menu);
    }

    @Override
    public void setAltToolbarTitle(CharSequence title) {
        altToolbar.setTitle(title);
    }

    @Override
    public void toggleAltToolbar(boolean show) {
        TeammatesBaseFragment current = (TeammatesBaseFragment) getCurrentFragment();
        if (show) toggleToolbar(false);
        else if (current != null) toggleToolbar(current.showsToolBar());

        altToolbar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void toggleToolbar(boolean show) {
        super.toggleToolbar(show);
        altToolbar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void toggleBottombar(boolean show) {
        if (bottombarHider == null) return;
        if (show) bottombarHider.show();
        else bottombarHider.hide();
    }

    @Override
    public void hideBottomSheet() {
        bottomSheetBehavior.setState(STATE_HIDDEN);
        restoreHiddenViewState();
    }

    @Override
    public void showBottomSheet(Args args) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager == null) return;

        BaseFragment toShow = args.getFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.bottom_sheet, toShow, toShow.getStableTag())
                .commit();

        bottomSheetToolbar.getMenu().clear();
        bottomSheetToolbar.inflateMenu(args.getMenuRes());
        bottomSheetToolbar.setTitle(args.getTitle());
        bottomSheetBehavior.setState(STATE_EXPANDED);
    }

    @Override
    public boolean showFragment(BaseFragment fragment) {
        hideBottomSheet();
        return super.showFragment(fragment);
    }

    private void route(@Nullable Bundle savedInstanceState, @NonNull Intent intent) {
        Model model = intent.getParcelableExtra(FEED_DEEP_LINK);
        BaseFragment route = null;

        if (model != null) route = route(
                () -> route(model, Chat.class, Chat::getTeam, ChatFragment::newInstance),
                () -> route(model, Event.class, event -> event, EventEditFragment::newInstance),
                () -> route(model, JoinRequest.class, JoinRequestEntity::getTeam, TeamMembersFragment::newInstance)
        );

        if (route != null) showFragment(route);
        else if (savedInstanceState == null) showFragment(FeedFragment.newInstance());
    }

    private void restoreHiddenViewState() {
        TeammatesBaseFragment fragment = (TeammatesBaseFragment) getCurrentFragment();
        if (fragment == null) return;

        fragment.togglePersistentUi();
        setFabClickListener(fragment);
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
