package com.mainstreetcode.teammates.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.baseclasses.TeammatesBaseActivity;
import com.mainstreetcode.teammates.fragments.main.HomeFragment;
import com.mainstreetcode.teammates.fragments.main.SettingsFragment;

public class MainActivity extends TeammatesBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startRegistrationActivity(this);
            return;
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onOptionsItemSelected);
        if (savedInstanceState == null) showFragment(HomeFragment.newInstance());
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
        }
        return super.onOptionsItemSelected(item);
    }

    public static void startRegistrationActivity(Activity activity) {
        Intent main = new Intent(activity, RegistrationActivity.class);
        activity.startActivity(main);
        activity.finish();
    }

}
