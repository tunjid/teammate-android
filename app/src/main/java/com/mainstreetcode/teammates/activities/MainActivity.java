package com.mainstreetcode.teammates.activities;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.baseclasses.TeammatesBaseActivity;
import com.mainstreetcode.teammates.fragments.headless.TeamPickerFragment;
import com.mainstreetcode.teammates.fragments.main.EventEditFragment;
import com.mainstreetcode.teammates.fragments.main.EventsFragment;
import com.mainstreetcode.teammates.fragments.main.HomeFragment;
import com.mainstreetcode.teammates.fragments.main.SettingsFragment;
import com.mainstreetcode.teammates.fragments.main.ChatFragment;
import com.mainstreetcode.teammates.fragments.main.TeamMediaFragment;
import com.mainstreetcode.teammates.fragments.main.TeamsFragment;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.model.Chat;
import com.mainstreetcode.teammates.notifications.TeammatesInstanceIdService;
import com.mainstreetcode.teammates.viewmodel.UserViewModel;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

public class MainActivity extends TeammatesBaseActivity {

    public static final String FEED_DEEP_LINK = "feed-deep-link";

    private BottomNavigationView bottomNavigationView;

    final FragmentManager.FragmentLifecycleCallbacks lifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v, Bundle savedInstanceState) {
            Menu menu = bottomNavigationView.getMenu();

            String t = f.getTag();

            if (t == null) return;
            int id = 0;

            if (t.contains(HomeFragment.class.getSimpleName())) id = R.id.action_home;
            else if (t.contains(EventsFragment.class.getSimpleName())) id = R.id.action_events;
            else if (t.contains(ChatFragment.class.getSimpleName())) id = R.id.action_messages;
            else if (t.contains(TeamMediaFragment.class.getSimpleName())) id = R.id.action_media;
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
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onOptionsItemSelected);

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
                TeamPickerFragment.request(this, R.id.request_event_team_pick);
                return true;
            case R.id.action_messages:
                TeamPickerFragment.request(this, R.id.request_chat_team_pick);
                return true;
            case R.id.action_media:
                TeamPickerFragment.request(this, R.id.request_media_team_pick);
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

    public static void startRegistrationActivity(Activity activity) {
        Intent main = new Intent(activity, RegistrationActivity.class);
        activity.startActivity(main);
        activity.finish();
    }
}
