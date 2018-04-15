package com.mainstreetcode.teammate.fragments.main;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.Marker;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.activities.MainActivity;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Event;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

import java.util.List;

import static com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.getLayoutParams;
import static com.mainstreetcode.teammate.viewmodel.LocationViewModel.PERMISSIONS_REQUEST_LOCATION;

public class PublicEventFragment extends MainActivityFragment {

    public static final int MAP_ZOOM = 10;

    private boolean leaveMap;
    private MapView mapView;

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

        mapView = root.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this::onMapReady);
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
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    requestLocation();
                }
                break;

        }
    }

    private void requestLocation() {
        disposables.add(locationViewModel.getLastLocation(this)
                .subscribe(latLng -> mapView.getMapAsync(map -> map.animateCamera(newLatLngZoom(latLng, MAP_ZOOM))), defaultErrorHandler));
    }

    @SuppressLint("MissingPermission")
    private void onMapReady(GoogleMap map) {
        int padding = getResources().getDimensionPixelSize(R.dimen.single_and_half_margin);

        map.setPadding(0, padding, 0, padding);
        map.setOnCameraIdleListener(() -> onMapIdle(map));
        map.setOnCameraMoveStartedListener(this::onCameraMoveStarted);
        map.setOnInfoWindowClickListener(this::onMarkerInfoWindowClicked);

        if (locationViewModel.hasPermission(this)) map.setMyLocationEnabled(true);
    }

    private void populateMap(GoogleMap map, List<Event> events) {
        if (leaveMap) return;
        map.clear();
        for (Event event : events) map.addMarker(event.getMarkerOptions()).setTag(event);
    }

    private void onMapIdle(GoogleMap map) {
        disposables.add(eventViewModel.getPublicEvents(map).subscribe(events -> populateMap(map, events), defaultErrorHandler));
    }

    private void onCameraMoveStarted(int reason) {
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
}
