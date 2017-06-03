package com.mainstreetcode.teammates.baseclasses;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseActivity;
import com.tunjid.androidbootstrap.core.view.ViewHider;

/**
 * Base Activity for the app
 * <p>
 * Created by Shemanigans on 6/1/17.
 */

public abstract class TeammatesBaseActivity extends BaseActivity
        implements OnApplyWindowInsetsListener {

    private boolean insetsApplied;
    private View insetView;
    private ViewHider toolbarHider;

    final FragmentManager.FragmentLifecycleCallbacks lifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v, Bundle savedInstanceState) {
            boolean isFullscreenFragment = isFullscreenFragment(f.getTag());

            insetView.setVisibility(isFullscreenFragment ? View.GONE : View.VISIBLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int color = isFullscreenFragment ? R.color.transparent : R.color.colorPrimaryDark;
                getWindow().setStatusBarColor(ContextCompat.getColor(TeammatesBaseActivity.this, color));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager().registerFragmentLifecycleCallbacks(lifecycleCallbacks, false);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        Toolbar toolbar = findViewById(R.id.toolbar);
        insetView = findViewById(R.id.inset_view);
        toolbarHider = new ViewHider(toolbar, ViewHider.TOP);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.content_view), this);
        }
    }

    public void toggleToolbar(boolean show) {
        if (show) toolbarHider.show();
        else toolbarHider.hide();
    }

    @Override
    public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
        if (insetsApplied) return insets;

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) insetView.getLayoutParams();
        params.height = insets.getSystemWindowInsetTop();

        insetsApplied = true;
        return insets;
    }

    protected boolean isFullscreenFragment(String tag) {
        return false;
    }
}
