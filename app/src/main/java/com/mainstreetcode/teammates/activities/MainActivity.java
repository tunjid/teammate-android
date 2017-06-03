package com.mainstreetcode.teammates.activities;

import android.os.Bundle;
import android.support.v4.view.OnApplyWindowInsetsListener;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.baseclasses.TeammatesBaseActivity;

public class MainActivity extends TeammatesBaseActivity
        implements OnApplyWindowInsetsListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
}
