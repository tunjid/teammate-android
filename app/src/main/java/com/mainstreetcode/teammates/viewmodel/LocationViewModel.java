package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.mainstreetcode.teammates.Application;
import com.mainstreetcode.teammates.util.TeammateException;

import io.reactivex.Single;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;

/**
 * ViewModel for signed in team
 * <p>
 * Created by Shemanigans on 6/4/17.
 */

public class LocationViewModel extends ViewModel {

    private static final int MAX_RESULTS = 1;

    public LocationViewModel() {
    }

    public Single<Address> fromPlace(Place place) {
        LatLng latLng = place.getLatLng();
        Geocoder geocoder = new Geocoder(Application.getInstance());

        return Single.fromCallable(() -> geocoder.getFromLocation(latLng.latitude, latLng.longitude, MAX_RESULTS))
                .map(addresses -> {
                    if (!addresses.isEmpty()) return addresses.get(0);
                    else throw new TeammateException("No address found");
                })
                .subscribeOn(io())
                .observeOn(mainThread());
    }
}
