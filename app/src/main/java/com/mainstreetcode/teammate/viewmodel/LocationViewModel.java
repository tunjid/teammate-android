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

package com.mainstreetcode.teammate.viewmodel;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.text.style.StyleSpan;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.util.InstantSearch;
import com.mainstreetcode.teammate.util.TeammateException;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;


public class LocationViewModel extends ViewModel {

    public static final int PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int MAX_RESULTS = 1;
    private static final StyleSpan CHARACTER_STYLE = new StyleSpan(Typeface.BOLD);

    private final Geocoder geocoder = new Geocoder(App.getInstance());
    private final PlacesClient client = Places.createClient(App.getInstance());

    public LocationViewModel() {}

    public Maybe<Address> fromMap(GoogleMap map) {
        LatLng location = map.getCameraPosition().target;
        return withGeocoder(it -> it.getFromLocation(location.latitude, location.longitude, MAX_RESULTS));
    }

    public Maybe<Address> fromAutoComplete(AutocompletePrediction prediction) {
        return withGeocoder(it -> it.getFromLocationName(prediction.getFullText(CHARACTER_STYLE).toString(), MAX_RESULTS));
    }

    @SuppressLint("MissingPermission")
    public Maybe<LatLng> getLastLocation(Fragment fragment) {
        if (!hasPermission(fragment)) {
            if (fragment.shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION))
                explainPermission(fragment);
            else requestPermission(fragment);
            return Maybe.empty();
        }

        String errorString = fragment.getString(R.string.event_public_location_unknown);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(fragment.requireActivity());

        return Maybe.create(emit -> client.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location == null) emit.onError(new TeammateException(errorString));
                    else emit.onSuccess(new LatLng(location.getLatitude(), location.getLongitude()));
                })
                .addOnCompleteListener(ignored -> emit.onComplete())
                .addOnFailureListener(emit::onError));
    }

    public InstantSearch<String, AutocompletePrediction> instantSearch() {
        return new InstantSearch<>(this::fromQuery, prediction -> Differentiable.fromCharSequence(prediction::getPlaceId));
    }

    public boolean hasPermission(Fragment fragment) {
        return SDK_INT < M || ContextCompat.checkSelfPermission(fragment.requireActivity(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(Fragment fragment) {
        fragment.requestPermissions(new String[]{ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
    }

    private void explainPermission(Fragment fragment) {
        new AlertDialog.Builder(fragment.getActivity())
                .setTitle(R.string.event_public_location_prompt)
                .setMessage(R.string.event_public_location_rationale)
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> requestPermission(fragment))
                .create()
                .show();
    }

    private Maybe<Address> withGeocoder(Function<Geocoder, List<Address>> addressFunction) {
        return Maybe.fromCallable(() -> addressFunction.apply(geocoder))
                .flatMap(addresses -> addresses.isEmpty() ? Maybe.empty() : Maybe.just(addresses.get(0)))
                .subscribeOn(io())
                .observeOn(mainThread());
    }

    private Single<List<AutocompletePrediction>> fromQuery(String query) {
        return Single.create(emitter -> client.findAutocompletePredictions(
                FindAutocompletePredictionsRequest.builder().setQuery(query).build())
                .addOnSuccessListener(response -> emitter.onSuccess(response.getAutocompletePredictions()))
                .addOnFailureListener(emitter::onError));
    }
}
