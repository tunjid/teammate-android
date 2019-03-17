package com.mainstreetcode.teammate.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.baseclasses.BottomSheetController;
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseActivity;
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseFragment;
import com.mainstreetcode.teammate.fragments.headless.TeamPickerFragment;
import com.mainstreetcode.teammate.fragments.main.BlankBottomSheetFragment;
import com.mainstreetcode.teammate.fragments.main.ChatFragment;
import com.mainstreetcode.teammate.fragments.main.DeclinedCompetitionsFragment;
import com.mainstreetcode.teammate.fragments.main.EventEditFragment;
import com.mainstreetcode.teammate.fragments.main.EventSearchFragment;
import com.mainstreetcode.teammate.fragments.main.EventsFragment;
import com.mainstreetcode.teammate.fragments.main.FeedFragment;
import com.mainstreetcode.teammate.fragments.main.GameFragment;
import com.mainstreetcode.teammate.fragments.main.HeadToHeadFragment;
import com.mainstreetcode.teammate.fragments.main.MediaFragment;
import com.mainstreetcode.teammate.fragments.main.MyEventsFragment;
import com.mainstreetcode.teammate.fragments.main.SettingsFragment;
import com.mainstreetcode.teammate.fragments.main.StatAggregateFragment;
import com.mainstreetcode.teammate.fragments.main.TeamMembersFragment;
import com.mainstreetcode.teammate.fragments.main.TeamsFragment;
import com.mainstreetcode.teammate.fragments.main.TournamentDetailFragment;
import com.mainstreetcode.teammate.fragments.main.UserEditFragment;
import com.mainstreetcode.teammate.model.Chat;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.notifications.TeammatesInstanceIdService;
import com.mainstreetcode.teammate.persistence.entity.JoinRequestEntity;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.mainstreetcode.teammate.util.nav.BottomNav;
import com.mainstreetcode.teammate.util.nav.NavDialogFragment;
import com.mainstreetcode.teammate.util.nav.NavItem;
import com.mainstreetcode.teammate.viewmodel.TeamViewModel;
import com.mainstreetcode.teammate.viewmodel.UserViewModel;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.functions.Supplier;
import com.tunjid.androidbootstrap.view.animator.ViewHider;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.arch.core.util.Function;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.disposables.CompositeDisposable;

import static android.view.View.GONE;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.listenForLayout;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.loadBitmapFromView;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.updateToolBar;
import static com.tunjid.androidbootstrap.view.animator.ViewHider.BOTTOM;
import static com.tunjid.androidbootstrap.view.util.ViewUtil.getLayoutParams;

public class MainActivity extends TeammatesBaseActivity
        implements BottomSheetController {

    public static final String FEED_DEEP_LINK = "feed-deep-link";
    public static final String BOTTOM_TOOLBAR_STATE = "BOTTOM_TOOLBAR_STATE";

    private int bottomNavHeight;

    @Nullable
    private ToolbarState bottomToolbarState;
    @Nullable
    private ViewHider bottomBarHider;
    private BottomNav bottomNav;

    private RecyclerView.RecycledViewPool inputRecycledPool;
    private BottomSheetBehavior bottomSheetBehavior;
    private ViewGroup bottomSheetContainer;
    private Toolbar bottomSheetToolbar;
    private Toolbar altToolbar;

    private UserViewModel userViewModel;

    private CompositeDisposable disposables;

    final FragmentManager.FragmentLifecycleCallbacks lifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, Bundle savedInstanceState) {
            if (isNotInMainFragmentContainer(v)) return;
            String t = f.getTag();

            if (t == null) return;
            int id = 0;

            if (t.contains(FeedFragment.class.getSimpleName())) id = R.id.action_home;
            else if (t.contains(EventsFragment.class.getSimpleName())) id = R.id.action_events;
            else if (t.contains(ChatFragment.class.getSimpleName())) id = R.id.action_messages;
            else if (t.contains(MediaFragment.class.getSimpleName())) id = R.id.action_media;
            else if (t.contains(TeamsFragment.class.getSimpleName())) id = R.id.action_team;

            bottomNav.highlight(id);
        }
    };

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(lifecycleCallbacks, false);

        userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        inputRecycledPool = new RecyclerView.RecycledViewPool();
        inputRecycledPool.setMaxRecycledViews(Item.INPUT, 10);

        if (!userViewModel.isSignedIn()) {
            startRegistrationActivity(this);
            return;
        }

        TeammatesInstanceIdService.updateFcmToken();

        disposables = new CompositeDisposable();

        altToolbar = findViewById(R.id.alt_toolbar);
        bottomSheetToolbar = findViewById(R.id.bottom_toolbar);
        bottomSheetContainer = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer);

        toolbar.setNavigationIcon(R.drawable.ic_supervisor_white_24dp);
        toolbar.setNavigationOnClickListener(view -> showNavOverflow());
        altToolbar.setOnMenuItemClickListener(this::onAltMenuItemSelected);
        bottomSheetToolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == STATE_HIDDEN) restoreHiddenViewState();
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });

        bottomNav = BottomNav.builder().setContainer(findViewById(R.id.bottom_navigation))
                .setListener(view -> onNavItemSelected(view.getId()))
                .setSwipeRunnable(this::showNavOverflow)
                .setNavItems(NavItem.create(R.id.action_home, R.string.home, R.drawable.ic_home_black_24dp),
                        NavItem.create(R.id.action_events, R.string.events, R.drawable.ic_event_white_24dp),
                        NavItem.create(R.id.action_messages, R.string.chats, R.drawable.ic_message_black_24dp),
                        NavItem.create(R.id.action_media, R.string.media, R.drawable.ic_video_library_black_24dp),
                        NavItem.create(R.id.action_tournaments, R.string.tourneys, R.drawable.ic_trophy_white_24dp))
                .createBottomNav();

        if (savedState != null) bottomToolbarState = savedState.getParcelable(BOTTOM_TOOLBAR_STATE);
        refreshBottomToolbar();

        route(savedState, getIntent());
        App.prime();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        View bottomNav = findViewById(R.id.bottom_navigation);
        ImageView bottomBarSnapshot = findViewById(R.id.bottom_nav_snapshot);
        listenForLayout(bottomNav, () -> getLayoutParams(bottomBarSnapshot).height = bottomNavHeight = bottomNav.getHeight());

        bottomBarHider = ViewHider.of(bottomBarSnapshot).setDuration(HIDER_DURATION)
                .setDirection(BOTTOM)
                .addStartRunnable(() -> {
                    TeammatesBaseFragment view = getCurrentFragment();
                    if (view == null || view.showsBottomNav()) return;

                    bottomBarSnapshot.setImageBitmap(loadBitmapFromView(bottomNav));
                    bottomNav.setVisibility(GONE);
                })
                .addEndRunnable(() -> {
                    TeammatesBaseFragment view = getCurrentFragment();
                    if (view == null || !view.showsBottomNav()) return;

                    bottomNav.setVisibility(View.VISIBLE);
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
    protected void onResume() {
        super.onResume();
        TeamViewModel teamViewModel = ViewModelProviders.of(this).get(TeamViewModel.class);

        disposables.add(teamViewModel.getTeamChangeFlowable()
                .flatMapSingle(team -> ViewHolderUtil.fetchRoundedDrawable(this,
                        team.getImageUrl(),
                        getResources().getDimensionPixelSize(R.dimen.double_margin),R.drawable.ic_supervisor_white_24dp))
                .subscribe(this::updateToolbarIcon, ErrorHandler.EMPTY));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return onNavItemSelected(item.getItemId());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(BOTTOM_TOOLBAR_STATE, bottomToolbarState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        disposables.clear();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() != STATE_HIDDEN) hideBottomSheet();
        else super.onBackPressed();
    }

    @Override
    public void updateAltToolbar(int menu, CharSequence title) {
        updateToolBar(altToolbar, menu, title);
    }

    @Override protected int adjustKeyboardPadding(int suggestion) {
        int padding = super.adjustKeyboardPadding(suggestion);
        if (padding != bottomInset) padding -= bottomNavHeight;
        return padding;
    }

    @Override
    public void toggleAltToolbar(boolean show) {
        TeammatesBaseFragment current = getCurrentFragment();
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
        if (bottomBarHider == null) return;
        if (show) bottomBarHider.show();
        else bottomBarHider.hide();
    }

    @Override
    public boolean isBottomSheetShowing() { return bottomSheetBehavior.getState() != STATE_HIDDEN; }

    @Override
    public void hideBottomSheet() {
        bottomSheetBehavior.setState(STATE_HIDDEN);
        restoreHiddenViewState();
    }

    @Override
    public void showBottomSheet(Args args) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager == null) return;

        clearTransientBars();

        int topPadding = TeammatesBaseActivity.topInset + getResources().getDimensionPixelSize(R.dimen.single_margin);
        bottomSheetContainer.setPadding(0, topPadding, 0, 0);

        BaseFragment toShow = args.getFragment();
        toShow.setEnterTransition(getBottomSheetTransition());
        toShow.setExitTransition(getBottomSheetTransition());

        fragmentManager.beginTransaction()
                .replace(R.id.bottom_sheet_view, toShow, toShow.getStableTag())
                .runOnCommit(() -> {
                    bottomToolbarState = args.getToolbarState();
                    bottomSheetBehavior.setState(STATE_EXPANDED);
                    refreshBottomToolbar();
                }).commit();
    }

    @Override
    public boolean showFragment(BaseFragment fragment) {
        hideBottomSheet();
        return super.showFragment(fragment);
    }

    private boolean onAltMenuItemSelected(MenuItem item) {
        Fragment current = getCurrentFragment();
        return current != null && current.onOptionsItemSelected(item);
    }

    public RecyclerView.RecycledViewPool getInputRecycledPool() {
        return inputRecycledPool;
    }

    private void showNavOverflow() {
        NavDialogFragment.newInstance().show(getSupportFragmentManager(), "");
    }

    private void updateToolbarIcon(Drawable drawable) {
        Drawable current = toolbar.getNavigationIcon();
        TransitionDrawable updated = new TransitionDrawable(current == null
                ? new Drawable[]{drawable}
                : new Drawable[]{current.getCurrent(), drawable});

        toolbar.setNavigationIcon(updated);
        if (current != null) updated.startTransition(HIDER_DURATION);
    }

    private boolean onNavItemSelected(@IdRes int id) {
        switch (id) {
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
            case R.id.action_tournaments:
                TeamPickerFragment.pick(this, R.id.request_tournament_team_pick);
                return true;
            case R.id.action_games:
                TeamPickerFragment.pick(this, R.id.request_game_team_pick);
                return true;
            case R.id.action_team:
                showFragment(TeamsFragment.newInstance());
                return true;
            case R.id.action_expand_home_nav:
                showNavOverflow();
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
            case R.id.action_head_to_head:
                showFragment(HeadToHeadFragment.newInstance());
                return true;
            case R.id.action_stats_aggregate:
                showFragment(StatAggregateFragment.newInstance());
                return true;
            case R.id.action_declined_competitions:
                showFragment(DeclinedCompetitionsFragment.newInstance());
                return true;
            case R.id.action_my_profile:
                showFragment(UserEditFragment.newInstance(userViewModel.getCurrentUser()));
                return true;
            default:
                return false;
        }
    }

    private Transition getBottomSheetTransition() {
        return new Fade().setDuration(250);
    }

    private void route(@Nullable Bundle savedInstanceState, @NonNull Intent intent) {
        Model model = intent.getParcelableExtra(FEED_DEEP_LINK);
        BaseFragment route = null;

        if (model != null) route = route(
                () -> route(model, Game.class, game -> game, GameFragment::newInstance),
                () -> route(model, Chat.class, Chat::getTeam, ChatFragment::newInstance),
                () -> route(model, Event.class, event -> event, EventEditFragment::newInstance),
                () -> route(model, JoinRequest.class, JoinRequestEntity::getTeam, TeamMembersFragment::newInstance),
                () -> route(model, Tournament.class, tournament -> tournament, TournamentDetailFragment::newInstance)
        );

        if (route != null) showFragment(route);
        else if (savedInstanceState == null) showFragment(FeedFragment.newInstance());
    }

    private void refreshBottomToolbar() {
        if (bottomToolbarState == null) return;
        bottomSheetToolbar.getMenu().clear();
        bottomSheetToolbar.inflateMenu(bottomToolbarState.getMenuRes());
        bottomSheetToolbar.setTitle(bottomToolbarState.getTitle());
    }

    private void restoreHiddenViewState() {
        TeammatesBaseFragment current = getCurrentFragment();
        if (current == null) return;

        Runnable onCommit = () -> {
            TeammatesBaseFragment post = getCurrentFragment();
            if (post != null && post.getView() != null) post.togglePersistentUi();
        };

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager == null) return;

        BaseFragment fragment = BlankBottomSheetFragment.newInstance();
        fragmentManager.beginTransaction()
                .replace(R.id.bottom_sheet_view, fragment, fragment.getStableTag())
                .runOnCommit(onCommit)
                .commit();
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
