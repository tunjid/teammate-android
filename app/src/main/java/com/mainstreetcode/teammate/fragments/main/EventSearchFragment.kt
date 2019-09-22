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
import com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.EventSearchRequestAdapter
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment
import com.mainstreetcode.teammate.baseclasses.WindowInsetsDriver
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.util.ExpandingToolbar
import com.mainstreetcode.teammate.util.Logger
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.util.updateTheme
import com.mainstreetcode.teammate.viewmodel.LocationViewModel.Companion.PERMISSIONS_REQUEST_LOCATION
import com.tunjid.androidbootstrap.view.util.InsetFlags

class EventSearchFragment : MainActivityFragment(R.layout.fragment_public_event), AddressPickerFragment.AddressPicker {

    private var leaveMap: Boolean = false

    private var mapView: MapView? = null
    private var expandingToolbar: ExpandingToolbar? = null

    override val showsFab: Boolean get() = locationViewModel.hasPermission(this)

    override val insetFlags: InsetFlags get() = NO_TOP

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        defaultUi(
                toolbarShows = false,
                fabShows = locationViewModel.hasPermission(this),
                fabIcon = R.drawable.ic_crosshairs_gps_white_24dp,
                fabText = R.string.event_my_location,
                bottomNavShows = false
        )

        view.findViewById<View>(R.id.status_bar_dimmer).layoutParams.height = WindowInsetsDriver.topInset

        scrollManager = ScrollManager.with<BaseViewHolder<*>>(view.findViewById(R.id.search_options))
                .withAdapter(EventSearchRequestAdapter(eventViewModel.eventRequest, object : EventSearchRequestAdapter.EventSearchAdapterListener {
                    override fun onLocationClicked() = this@EventSearchFragment.pickPlace()
                }))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withRecycledViewPool(inputRecycledViewPool())
                .withLinearLayoutManager()
                .build()

        mapView = view.findViewById(R.id.map_view)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this::onMapReady)

        expandingToolbar = ExpandingToolbar.create(view.findViewById(R.id.card_view_wrapper)) { mapView?.getMapAsync(this::fetchPublicEvents) }
        expandingToolbar?.setTitle(R.string.event_public_search)
        expandingToolbar?.setTitleIcon(false)
    }

    override fun onResume() {
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
        mapView = null
        expandingToolbar = null
        super.onDestroyView()
    }

    override fun onLowMemory() {
        mapView?.onLowMemory()
        super.onLowMemory()
    }

    override fun onClick(view: View) {
        if (view.id == R.id.fab)
            disposables.add(locationViewModel.getLastLocation(this)
                    .subscribe({ location -> onLocationFound(location, true) }, defaultErrorHandler::invoke))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        val permissionGranted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        if (permissionGranted && requestCode == PERMISSIONS_REQUEST_LOCATION) requestLocation()
    }

    override fun onAddressPicked(address: Address) =
            onLocationFound(LatLng(address.latitude, address.longitude), true)

    private fun fetchPublicEvents(map: GoogleMap) {
        disposables.add(eventViewModel.getPublicEvents(map).subscribe({ events -> populateMap(map, events) }, defaultErrorHandler::invoke))
    }

    private fun requestLocation() {
        updateUi(fabShows = locationViewModel.hasPermission(this))

        val lastLocation = eventViewModel.lastPublicSearchLocation

        if (lastLocation != null) onLocationFound(lastLocation, false)
        else disposables.add(locationViewModel.getLastLocation(this)
                .subscribe({ location -> onLocationFound(location, true) }, defaultErrorHandler::invoke))
    }

    private fun onLocationFound(location: LatLng, animate: Boolean) {
        mapView?.getMapAsync { map ->
            if (animate) map.animateCamera(newLatLngZoom(location, MAP_ZOOM.toFloat()))
            else map.moveCamera(newLatLngZoom(location, MAP_ZOOM.toFloat()))
        }
    }

    private fun onMapReady(map: GoogleMap) {
        map.updateTheme(requireContext())
        map.setOnCameraIdleListener { onMapIdle(map) }
        map.setOnCameraMoveStartedListener(this::onCameraMoveStarted)
        map.setOnInfoWindowClickListener(this::onMarkerInfoWindowClicked)
    }

    private fun populateMap(map: GoogleMap, events: List<Event>) {
        if (leaveMap) return
        map.clear()
        for (event in events) map.addMarker(event.markerOptions).tag = event
    }

    private fun onMapIdle(map: GoogleMap) {
        fetchPublicEvents(map)
        disposables.add(locationViewModel.fromMap(map).subscribe(this::onAddressFound, defaultErrorHandler::invoke))
        scrollManager.notifyDataSetChanged()
    }

    private fun onCameraMoveStarted(reason: Int) {
        if (scrollManager.recyclerView?.visibility == View.VISIBLE)
            expandingToolbar?.changeVisibility(true)

        when (reason) {
            GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION -> leaveMap = true
            GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE,
            GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION -> leaveMap = false
        }
    }

    private fun onMarkerInfoWindowClicked(marker: Marker) {
        val tag = marker.tag as? Event ?: return
        navigator.show(EventEditFragment.newInstance(tag))
    }

    private fun onAddressFound(address: Address) {
        eventViewModel.eventRequest.setAddress(address)
        scrollManager.notifyItemChanged(0)
    }

    companion object {

        private const val MAP_ZOOM = 10

        fun newInstance(): EventSearchFragment = EventSearchFragment().apply {
            arguments = Bundle()
            setEnterExitTransitions()
        }
    }
}
