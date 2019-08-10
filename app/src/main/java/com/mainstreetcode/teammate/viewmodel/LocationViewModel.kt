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

package com.mainstreetcode.teammate.viewmodel

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.Address
import android.location.Geocoder
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import android.text.style.StyleSpan
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.util.InstantSearch
import com.mainstreetcode.teammate.util.TeammateException
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io


class LocationViewModel : ViewModel() {

    private val geocoder = Geocoder(App.getInstance())
    private val client = Places.createClient(App.getInstance())

    fun fromMap(map: GoogleMap): Maybe<Address> {
        val location = map.cameraPosition.target
        return withGeocoder { it.getFromLocation(location.latitude, location.longitude, MAX_RESULTS) }
    }

    fun fromAutoComplete(prediction: AutocompletePrediction): Maybe<Address> =
            withGeocoder { it.getFromLocationName(prediction.getFullText(CHARACTER_STYLE).toString(), MAX_RESULTS) }

    @SuppressLint("MissingPermission")
    fun getLastLocation(fragment: Fragment): Maybe<LatLng> {
        if (!hasPermission(fragment)) {
            if (fragment.shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) explainPermission(fragment)
            else requestPermission(fragment)
            return Maybe.empty()
        }

        val errorString = fragment.getString(R.string.event_public_location_unknown)
        val client = LocationServices.getFusedLocationProviderClient(fragment.requireActivity())

        return Maybe.create { emit ->
            client.lastLocation
                    .addOnSuccessListener { location ->
                        if (location == null) emit.onError(TeammateException(errorString))
                        else emit.onSuccess(LatLng(location.latitude, location.longitude))
                    }
                    .addOnCompleteListener { emit.onComplete() }
                    .addOnFailureListener(emit::onError)
        }
    }

    fun instantSearch(): InstantSearch<String, AutocompletePrediction> =
            InstantSearch(this::fromQuery) { Differentiable.fromCharSequence(it::getPlaceId) }

    fun hasPermission(fragment: Fragment): Boolean =
            SDK_INT < M || ContextCompat.checkSelfPermission(fragment.requireActivity(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission(fragment: Fragment) =
            fragment.requestPermissions(arrayOf(ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_LOCATION)

    private fun explainPermission(fragment: Fragment) {
        AlertDialog.Builder(fragment.activity)
                .setTitle(R.string.event_public_location_prompt)
                .setMessage(R.string.event_public_location_rationale)
                .setPositiveButton(R.string.yes) { _, _ -> requestPermission(fragment) }
                .create()
                .show()
    }

    private fun withGeocoder(addressFunction: (Geocoder) -> List<Address>) =
            Maybe.fromCallable { addressFunction.invoke(geocoder) }
                    .flatMap { addresses -> if (addresses.isEmpty()) Maybe.empty() else Maybe.just(addresses[0]) }
                    .subscribeOn(io())
                    .observeOn(mainThread())

    private fun fromQuery(query: String): Single<List<AutocompletePrediction>> = Single.create { emitter ->
        client.findAutocompletePredictions(
                FindAutocompletePredictionsRequest.builder().setQuery(query).build())
                .addOnSuccessListener { response -> emitter.onSuccess(response.autocompletePredictions) }
                .addOnFailureListener(emitter::onError)
    }

    companion object {

        const val PERMISSIONS_REQUEST_LOCATION = 99
        private const val MAX_RESULTS = 1
        private val CHARACTER_STYLE = StyleSpan(Typeface.BOLD)
    }
}
