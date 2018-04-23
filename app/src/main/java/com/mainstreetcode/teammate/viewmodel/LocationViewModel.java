package com.mainstreetcode.teammate.viewmodel;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModel;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.util.TeammateException;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.processors.PublishProcessor;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;


public class LocationViewModel extends ViewModel {

    public static final int PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int MAX_RESULTS = 1;

    public LocationViewModel() {}

    public Single<Address> fromPlace(Place place) {
        LatLng latLng = place.getLatLng();
        Geocoder geocoder = new Geocoder(App.getInstance());

        return Single.fromCallable(() -> geocoder.getFromLocation(latLng.latitude, latLng.longitude, MAX_RESULTS))
                .map(addresses -> {
                    if (!addresses.isEmpty()) return addresses.get(0);
                    else throw new TeammateException("No address found");
                })
                .subscribeOn(io())
                .observeOn(mainThread());
    }

    @SuppressLint("MissingPermission")
    public Maybe<LatLng> getLastLocation(Fragment fragment) {
        if (!hasPermission(fragment)) {
            if (fragment.shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION))
                explainPermission(fragment);
            else requestPermission(fragment);
            return Maybe.empty();
        }

        PublishProcessor<LatLng> locationPublishProcessor = PublishProcessor.create();
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(fragment.requireActivity());

        client.getLastLocation().addOnSuccessListener(location -> locationPublishProcessor.onNext(new LatLng(location.getLatitude(), location.getLongitude())))
                .addOnCompleteListener(ignored -> locationPublishProcessor.onComplete())
                .addOnFailureListener(locationPublishProcessor::onError);

        return locationPublishProcessor.firstElement();
    }

    public boolean hasPermission(Fragment fragment) {
        return SDK_INT >= M && ContextCompat.checkSelfPermission(fragment.requireActivity(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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
}
