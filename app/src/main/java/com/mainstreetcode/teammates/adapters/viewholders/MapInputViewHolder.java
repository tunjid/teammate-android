package com.mainstreetcode.teammates.adapters.viewholders;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.EventEditAdapter;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Item;

/**
 * ViewHolder for selecting locations from a map
 */
public class MapInputViewHolder extends InputViewHolder<EventEditAdapter.EditAdapterListener>
        implements View.OnClickListener {

    private final MapView mapView;
    private final Button addButton;

    public MapInputViewHolder(View itemView, EventEditAdapter.EditAdapterListener listener) {
        super(itemView, true);
        this.adapterListener = listener;
        mapView = itemView.findViewById(R.id.map_view);
        addButton = itemView.findViewById(R.id.add);

        mapView.onCreate(new Bundle());
        addButton.setOnClickListener(this);
        itemView.findViewById(R.id.click_view).setOnClickListener(this);
    }

    @Override
    public void bind(Item item) {
        super.bind(item);

        if (!(item.getItemizedObject() instanceof Event)) return;
        Event event = (Event) item.getItemizedObject();

        LatLng location = event.getLocation();

        if (location == null) {
            addButton.setVisibility(View.VISIBLE);
            mapView.setVisibility(View.GONE);
        }
        else {
            addButton.setVisibility(View.GONE);
            mapView.setVisibility(View.VISIBLE);
            mapView.getMapAsync(googleMap -> {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(location);
                googleMap.moveCamera(cameraUpdate);
                googleMap.clear();
                googleMap.addMarker(new MarkerOptions().position(location));
            });
        }
    }

    @Override
    public void onClick(View view) {
        adapterListener.onLocationClicked();
    }

}
