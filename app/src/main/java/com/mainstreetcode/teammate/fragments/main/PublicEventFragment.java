package com.mainstreetcode.teammate.fragments.main;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.transition.AutoTransition;
import android.support.transition.TransitionManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.TextViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.Marker;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.activities.MainActivity;
import com.mainstreetcode.teammate.adapters.PublicEventRequestAdapter;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

import java.util.List;

import static com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.getLayoutParams;
import static com.mainstreetcode.teammate.viewmodel.LocationViewModel.PERMISSIONS_REQUEST_LOCATION;

public class PublicEventFragment extends MainActivityFragment {

    public static final int MAP_ZOOM = 10;

    private boolean leaveMap;

    private MapView mapView;
    private TextView searchTitle;

    public static PublicEventFragment newInstance() {
        PublicEventFragment fragment = new PublicEventFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        fragment.setEnterExitTransitions();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_public_event, container, false);
        getLayoutParams(root.findViewById(R.id.status_bar_dimmer)).height = MainActivity.topInset;

        scrollManager = ScrollManager.withRecyclerView(root.findViewById(R.id.search_options))
                .withAdapter(new PublicEventRequestAdapter(eventViewModel.getEventRequest()))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .build();

        searchTitle = root.findViewById(R.id.search_title);
        searchTitle.setOnClickListener(clicked -> changeVisibility(searchTitle, scrollManager.getRecyclerView()));

        mapView = root.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this::onMapReady);

        setIcons(false, searchTitle);
        return root;
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onStart() {
        mapView.onStart();
        super.onStart();
        requestLocation();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        mapView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        mapView.onDestroy();
        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
        mapView.onLowMemory();
        super.onLowMemory();
    }

    @Override
    public boolean showsToolBar() { return false; }

    @Override
    public boolean showsBottomNav() { return false; }

    @Override
    public boolean[] insetState() {return NONE;}

    @Nullable
    @Override
    public FragmentTransaction provideFragmentTransaction(BaseFragment fragmentTo) {
        return super.provideFragmentTransaction(fragmentTo);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        boolean permissionGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (permissionGranted && requestCode == PERMISSIONS_REQUEST_LOCATION) requestLocation();
    }

    private void requestLocation() {
        disposables.add(locationViewModel.getLastLocation(this)
                .subscribe(latLng -> mapView.getMapAsync(map -> map.animateCamera(newLatLngZoom(latLng, MAP_ZOOM))), defaultErrorHandler));
    }

    private void onMapReady(GoogleMap map) {
        map.setOnCameraIdleListener(() -> onMapIdle(map));
        map.setOnCameraMoveStartedListener(this::onCameraMoveStarted);
        map.setOnInfoWindowClickListener(this::onMarkerInfoWindowClicked);
    }

    private void populateMap(GoogleMap map, List<Event> events) {
        if (leaveMap) return;
        map.clear();
        for (Event event : events) map.addMarker(event.getMarkerOptions()).setTag(event);
    }

    private void onMapIdle(GoogleMap map) {
        disposables.add(eventViewModel.getPublicEvents(map).subscribe(events -> populateMap(map, events), defaultErrorHandler));
        scrollManager.notifyDataSetChanged();
    }

    private void onCameraMoveStarted(int reason) {
        if (scrollManager.getRecyclerView().getVisibility() == View.VISIBLE)
            changeVisibility(true, searchTitle, scrollManager.getRecyclerView());

        switch (reason) {
            case GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION:
                leaveMap = true;
                break;
            case GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE:
            case GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION:
                leaveMap = false;
                break;
        }
    }

    private void onMarkerInfoWindowClicked(Marker marker) {
        Object tag = marker.getTag();
        if (tag == null || !(tag instanceof Event)) return;
        showFragment(EventEditFragment.newInstance((Event) tag));
    }

    @SuppressLint("ResourceAsColor")
    private void setIcons(boolean isDown, TextView... textViews) {
        int resVal = isDown ? R.drawable.anim_vect_down_to_right_arrow : R.drawable.anim_vect_right_to_down_arrow;

        for (TextView textView : textViews) {
            Drawable icon = AnimatedVectorDrawableCompat.create(textView.getContext(), resVal);
            if (icon != null) {
                TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(textView, null, null, icon, null);
            }
        }
    }

    private void changeVisibility(boolean inVisible, TextView clicked, View... changing) {
        TransitionManager.beginDelayedTransition((ViewGroup) scrollManager.getRecyclerView().getParent(), new AutoTransition());

        setIcons(inVisible, clicked);

        AnimatedVectorDrawableCompat animatedDrawable = (AnimatedVectorDrawableCompat)
                TextViewCompat.getCompoundDrawablesRelative(clicked)[2];

        animatedDrawable.start();

        int visibility = inVisible ? View.GONE : View.VISIBLE;
        for (View view : changing) view.setVisibility(visibility);
    }

    private void changeVisibility(TextView clicked, View... changing) {
        boolean visible = changing[0].getVisibility() == View.VISIBLE;
        changeVisibility(visible, clicked, changing);
    }
}
