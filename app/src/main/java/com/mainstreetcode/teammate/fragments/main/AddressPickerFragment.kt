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
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.AutoCompleteAdapter
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment
import com.mainstreetcode.teammate.databinding.FragmentPlacePickerBinding
import com.mainstreetcode.teammate.util.InstantSearch
import com.mainstreetcode.teammate.util.Logger
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.util.fullName
import com.mainstreetcode.teammate.util.setMaterialOverlay
import com.mainstreetcode.teammate.util.updateTheme
import com.mainstreetcode.teammate.viewmodel.LocationViewModel.Companion.PERMISSIONS_REQUEST_LOCATION
import java.util.concurrent.atomic.AtomicReference

class AddressPickerFragment : MainActivityFragment(R.layout.fragment_place_picker) {

    private var canQueryMap: Boolean = false

    private lateinit var instantSearch: InstantSearch<String, AutocompletePrediction>
    private val currentAddress = AtomicReference<Address>()
    private var mapView: MapView? = null
    private var location: TextView? = null

    override val showsFab: Boolean get() = currentAddress.get() != null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instantSearch = locationViewModel.instantSearch()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = FragmentPlacePickerBinding.bind(view).run {
        this@AddressPickerFragment.mapView = mapView
        this@AddressPickerFragment.location = location

        defaultUi(
                toolbarShows = false,
                fabText = R.string.place_picker_pick,
                fabIcon = R.drawable.ic_check_white_24dp,
                fabShows = showsFab
        )
        scrollManager = ScrollManager.with<BaseViewHolder<*>>(searchPredictions)
                .withAdapter(AutoCompleteAdapter(instantSearch.currentItems, object : AutoCompleteAdapter.AdapterListener {
                    override fun onPredictionClicked(prediction: AutocompletePrediction) {
                        searchField.setQuery("", false)
                        searchField.clearFocus()
                        searchPredictions.visibility = View.GONE
                        mapViewContainer.visibility = View.VISIBLE
                        disposables.add(locationViewModel.fromAutoComplete(prediction)
                                .subscribe(this@AddressPickerFragment::onMapAddressFound, defaultErrorHandler::invoke))
                    }
                }))
                .withInconsistencyHandler(this@AddressPickerFragment::onInconsistencyDetected)
                .withLinearLayoutManager()
                .build()

        searchField.setIconifiedByDefault(false)
        searchField.isIconified = false
        searchField.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean = false

            override fun onQueryTextChange(query: String): Boolean {
                if (query.isBlank()) return true

                instantSearch.postSearch(query)
                mapViewContainer.visibility = if (query.isBlank()) View.VISIBLE else View.GONE
                searchPredictions.visibility = if (query.isBlank()) View.GONE else View.VISIBLE
                return true
            }
        })

        location.setMaterialOverlay(resources.getDimensionPixelSize(R.dimen.half_margin).toFloat())
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this@AddressPickerFragment::onMapReady) ?: Unit
    }

    override fun onResume() {
        disposables.add(instantSearch.subscribe().subscribe(scrollManager::onDiff, defaultErrorHandler::invoke))
        mapView?.onResume()
        super.onResume()
    }

    override fun onStart() {
        mapView?.onStart()
        super.onStart()
        requestLocation()
    }

    override fun onPause() {
        mapView?.onPause()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        try {
            mapView?.onSaveInstanceState(outState)
        } catch (e: Exception) {
            Logger.log(stableTag, "Error in mapview onSaveInstanceState", e)
        }

        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        mapView?.onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        mapView?.onDestroy()
        super.onDestroyView()
    }

    override fun onLowMemory() {
        mapView?.onLowMemory()
        super.onLowMemory()
    }

    override fun onClick(view: View) {
        if (view.id == R.id.fab) {
            if (targetFragment !is AddressPicker) return
            val current = currentAddress.get()

            if (current != null) (targetFragment as AddressPicker).onAddressPicked(current)
            bottomSheetDriver.hideBottomSheet()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        val permissionGranted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        if (permissionGranted && requestCode == PERMISSIONS_REQUEST_LOCATION) requestLocation()
    }

    private fun requestLocation() {
        disposables.add(locationViewModel.getLastLocation(this)
                .subscribe(this::onLocationFound, defaultErrorHandler::invoke))
    }

    private fun onLocationFound(location: LatLng) =
            mapView?.getMapAsync { map -> map.animateCamera(newLatLngZoom(location, MAP_ZOOM.toFloat())) }
                    ?: Unit

    private fun onMapReady(map: GoogleMap) {
        map.updateTheme(requireContext())
        map.setOnCameraIdleListener { onMapIdle(map) }
        map.setOnCameraMoveStartedListener(this::onCameraMoveStarted)
    }

    private fun onMapIdle(map: GoogleMap) {
        if (canQueryMap)
            disposables.add(locationViewModel.fromMap(map).subscribe(this::onAddressFound, defaultErrorHandler::invoke))
    }

    private fun onCameraMoveStarted(reason: Int) = when (reason) {
        GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION,
        GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION -> canQueryMap = false
        GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE -> canQueryMap = true
        else -> Unit
    }

    private fun onAddressFound(address: Address) {
        scrollManager.recyclerView?.visibility = View.GONE
        location?.text = address.fullName
        location?.visibility = View.VISIBLE
        currentAddress.set(address)

        updateUi(fabShows = showsFab)
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
