package com.mainstreetcode.teammates.activities;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.baseclasses.TeammatesBaseActivity;
import com.mainstreetcode.teammates.fragments.headless.TeamMediaPickerFragment;
import com.mainstreetcode.teammates.fragments.main.EventEditFragment;
import com.mainstreetcode.teammates.fragments.main.EventsFragment;
import com.mainstreetcode.teammates.fragments.main.HomeFragment;
import com.mainstreetcode.teammates.fragments.main.SettingsFragment;
import com.mainstreetcode.teammates.fragments.main.TeamChatFragment;
import com.mainstreetcode.teammates.fragments.main.TeamChatRoomFragment;
import com.mainstreetcode.teammates.fragments.main.TeamsFragment;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.model.TeamChat;
import com.mainstreetcode.teammates.viewmodel.UserViewModel;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

public class MainActivity extends TeammatesBaseActivity {

    public static final String FEED_DEEP_LINK = "feed-deep-link";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UserViewModel userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);

        if (!userViewModel.isSignedIn()) {
            startRegistrationActivity(this);
            return;
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onOptionsItemSelected);

        route(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_home:
                showFragment(HomeFragment.newInstance());
                return true;
            case R.id.action_settings:
                showFragment(SettingsFragment.newInstance());
                return true;
            case R.id.action_events:
                showFragment(EventsFragment.newInstance());
                return true;
            case R.id.action_team:
                showFragment(TeamsFragment.newInstance());
                return true;
            case R.id.action_messages:
                showFragment(TeamChatRoomFragment.newInstance());
                return true;
            case R.id.action_media:
                TeamMediaPickerFragment.request(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void route(Bundle savedInstanceState) {
        Intent intent = getIntent();
        Model model = intent.getParcelableExtra(FEED_DEEP_LINK);

        BaseFragment route = null;

        if (model != null) {
            if (model instanceof Event) route = EventEditFragment.newInstance((Event) model);
            if (model instanceof TeamChat) route = TeamChatFragment.newInstance(((TeamChat) model).getChatRoom());
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
