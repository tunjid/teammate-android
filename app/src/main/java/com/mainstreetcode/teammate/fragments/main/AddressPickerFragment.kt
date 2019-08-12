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

package com.mainstreetcode.teammate.fragments.main

import android.content.pm.PackageManager
import android.location.Address
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.AutoCompleteAdapter
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment
import com.mainstreetcode.teammate.util.InstantSearch
import com.mainstreetcode.teammate.util.Logger
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.util.fullName
import com.mainstreetcode.teammate.viewmodel.LocationViewModel.Companion.PERMISSIONS_REQUEST_LOCATION
import java.util.concurrent.atomic.AtomicReference

class AddressPickerFragment : MainActivityFragment() {

    private var canQueryMap: Boolean = false

    private lateinit var mapView: MapView
    private lateinit var location: TextView
    private lateinit var instantSearch: InstantSearch<String, AutocompletePrediction>
    private val currentAddress = AtomicReference<Address>()

    override val fabStringResource: Int
        @StringRes
        get() = R.string.place_picker_pick

    override val fabIconResource: Int
        @DrawableRes
        get() = R.drawable.ic_check_white_24dp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instantSearch = locationViewModel.instantSearch()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_place_picker, container, false)
        val mapContainer = root.findViewById<ViewGroup>(R.id.map_view_container)
        val searchView = root.findViewById<SearchView>(R.id.search_field)
        val recyclerView = root.findViewById<RecyclerView>(R.id.search_predictions)
        location = root.findViewById(R.id.location)

        scrollManager = ScrollManager.with<BaseViewHolder<*>>(recyclerView)
                .withAdapter(AutoCompleteAdapter(instantSearch.currentItems, object : AutoCompleteAdapter.AdapterListener {
                    override fun onPredictionClicked(prediction: AutocompletePrediction) {
                        searchView.setQuery("", false)
                        searchView.clearFocus()
                        recyclerView.visibility = View.GONE
                        mapContainer.visibility = View.VISIBLE
                        disposables.add(locationViewModel.fromAutoComplete(prediction)
                                .subscribe(this@AddressPickerFragment::onMapAddressFound, defaultErrorHandler::accept))
                    }
                }))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .build()

        searchView.setIconifiedByDefault(false)
        searchView.isIconified = false
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean = false

            override fun onQueryTextChange(query: String): Boolean {
                if (TextUtils.isEmpty(query)) return true

                instantSearch.postSearch(query)
                mapContainer.visibility = if (TextUtils.isEmpty(query)) View.VISIBLE else View.GONE
                recyclerView.visibility = if (TextUtils.isEmpty(query)) View.GONE else View.VISIBLE
                return true
            }
        })

        mapView = root.findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this::onMapReady)

        return root
    }

    override fun onResume() {
        disposables.add(instantSearch.subscribe().subscribe(scrollManager::onDiff, defaultErrorHandler::accept))
        mapView.onResume()
        super.onResume()
    }

    override fun onStart() {
        mapView.onStart()
        super.onStart()
        requestLocation()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        try {
            mapView.onSaveInstanceState(outState)
        } catch (e: Exception) {
            Logger.log(stableTag, "Error in mapview onSaveInstanceState", e)
        }

        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        mapView.onDestroy()
        super.onDestroyView()
    }

    override fun onLowMemory() {
        mapView.onLowMemory()
        super.onLowMemory()
    }

    override fun showsToolBar(): Boolean = false

    override fun showsBottomNav(): Boolean = true

    override fun showsFab(): Boolean = currentAddress.get() != null

    override fun onClick(view: View) {
        if (view.id == R.id.fab) {
            if (targetFragment !is AddressPicker) return
            val current = currentAddress.get()

            if (current != null) (targetFragment as AddressPicker).onAddressPicked(current)
            hideBottomSheet()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        val permissionGranted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        if (permissionGranted && requestCode == PERMISSIONS_REQUEST_LOCATION) requestLocation()
    }

    private fun requestLocation() {
        disposables.add(locationViewModel.getLastLocation(this)
                .subscribe(this::onLocationFound, defaultErrorHandler::accept))
    }

    private fun onLocationFound(location: LatLng) =
            mapView.getMapAsync { map -> map.animateCamera(newLatLngZoom(location, MAP_ZOOM.toFloat())) }

    private fun onMapReady(map: GoogleMap) {
        map.setOnCameraIdleListener { onMapIdle(map) }
        map.setOnCameraMoveStartedListener(this::onCameraMoveStarted)
    }

    private fun onMapIdle(map: GoogleMap) {
        if (canQueryMap)
            disposables.add(locationViewModel.fromMap(map).subscribe(this::onAddressFound, defaultErrorHandler::accept))
    }

    private fun onCameraMoveStarted(reason: Int) {
        when (reason) {
            GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION,
            GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION -> canQueryMap = false
            GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE -> canQueryMap = true
        }
    }

    private fun onAddressFound(address: Address) {
        scrollManager.recyclerView.visibility = View.GONE
        location.text = address.fullName
        location.visibility = View.VISIBLE
        currentAddress.set(address)

        togglePersistentUi()
    }

    private fun onMapAddressFound(address: Address) {
        onLocationFound(LatLng(address.latitude, address.longitude))
        onAddressFound(address)
    }

    interface AddressPicker {
        fun onAddressPicked(address: Address)
    }

    companion object {

        private const val MAP_ZOOM = 10

        fun newInstance(): AddressPickerFragment = AddressPickerFragment().apply {
            arguments = Bundle()
            setEnterExitTransitions()
        }
    }
}
