/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.fragments.main;

import android.content.pm.PackageManager;
import android.location.Address;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.AutoCompleteAdapter;
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.util.InstantSearch;
import com.mainstreetcode.teammate.util.Logger;
import com.mainstreetcode.teammate.util.ScrollManager;

import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import static com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom;
import static com.mainstreetcode.teammate.util.ModelUtils.nameAddress;
import static com.mainstreetcode.teammate.viewmodel.LocationViewModel.PERMISSIONS_REQUEST_LOCATION;

public class AddressPickerFragment extends MainActivityFragment {

    private static final int MAP_ZOOM = 10;

    private boolean canQueryMap;

    private MapView mapView;
    private TextView location;
    private InstantSearch<String, AutocompletePrediction> instantSearch;
    private AtomicReference<Address> currentAddress = new AtomicReference<>();

    public static AddressPickerFragment newInstance() {
        AddressPickerFragment fragment = new AddressPickerFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        fragment.setEnterExitTransitions();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instantSearch = locationViewModel.instantSearch();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_place_picker, container, false);
        ViewGroup mapContainer = root.findViewById(R.id.map_view_container);
        SearchView searchView = root.findViewById(R.id.search_field);
        RecyclerView recyclerView = root.findViewById(R.id.search_predictions);
        location = root.findViewById(R.id.location);

        scrollManager = ScrollManager.<BaseViewHolder>with(recyclerView)
                .withAdapter(new AutoCompleteAdapter(instantSearch.getCurrentItems(), prediction -> {
                    searchView.setQuery("", false);
                    searchView.clearFocus();
                    recyclerView.setVisibility(View.GONE);
                    mapContainer.setVisibility(View.VISIBLE);
                    disposables.add(locationViewModel.fromAutoComplete(prediction).subscribe(this::onMapAddressFound, defaultErrorHandler));
                }))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .build();

        searchView.setIconifiedByDefault(false);
        searchView.setIconified(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }

            @Override public boolean onQueryTextChange(String query) {
                if (TextUtils.isEmpty(query)) return true;

                instantSearch.postSearch(query);
                mapContainer.setVisibility(TextUtils.isEmpty(query) ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(TextUtils.isEmpty(query) ? View.GONE : View.VISIBLE);
                return true;
            }
        });

        mapView = root.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this::onMapReady);

        return root;
    }

    @Override
    public void onResume() {
        disposables.add(instantSearch.subscribe().subscribe(diffResult -> scrollManager.onDiff(diffResult), defaultErrorHandler));
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
        try { mapView.onSaveInstanceState(outState);}
        catch (Exception e) {Logger.log(getStableTag(), "Error in mapview onSaveInstanceState", e);}
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
        mapView = null;
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
    public boolean showsBottomNav() { return true; }

    @Override
    public boolean showsFab() { return currentAddress.get() != null; }

    @Override
    @StringRes
    protected int getFabStringResource() { return R.string.place_picker_pick; }

    @Override
    @DrawableRes
    protected int getFabIconResource() { return R.drawable.ic_check_white_24dp; }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab) {
            if (!(getTargetFragment() instanceof AddressPicker)) return;
            Address current = currentAddress.get();

            if (current != null) ((AddressPicker) getTargetFragment()).onAddressPicked(current);
            hideBottomSheet();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean permissionGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (permissionGranted && requestCode == PERMISSIONS_REQUEST_LOCATION) requestLocation();
    }

    private void requestLocation() {
        disposables.add(locationViewModel.getLastLocation(this)
                .subscribe(this::onLocationFound, defaultErrorHandler));
    }

    private void onLocationFound(LatLng location) {
        mapView.getMapAsync(map -> map.animateCamera(newLatLngZoom(location, MAP_ZOOM)));
    }

    private void onMapReady(GoogleMap map) {
        map.setOnCameraIdleListener(() -> onMapIdle(map));
        map.setOnCameraMoveStartedListener(this::onCameraMoveStarted);
    }

    private void onMapIdle(GoogleMap map) {
        if (canQueryMap)
            disposables.add(locationViewModel.fromMap(map).subscribe(this::onAddressFound, defaultErrorHandler));
    }

    private void onCameraMoveStarted(int reason) {
        switch (reason) {
            case GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION:
            case GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION:
                canQueryMap = false;
                break;
            case GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE:
                canQueryMap = true;
                break;
        }
    }

    private void onAddressFound(Address address) {
        scrollManager.getRecyclerView().setVisibility(View.GONE);
        location.setText(nameAddress(address));
        location.setVisibility(View.VISIBLE);
        currentAddress.set(address);

        togglePersistentUi();
    }

    private void onMapAddressFound(Address address) {
        onLocationFound(new LatLng(address.getLatitude(), address.getLongitude()));
        onAddressFound(address);
    }

    public interface AddressPicker {
        void onAddressPicked(Address address);
    }
}
