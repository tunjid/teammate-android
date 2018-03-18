package com.mainstreetcode.teammate.adapters.viewholders;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.EventEditAdapter;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Item;

/**
 * ViewHolder for selecting {@link com.mainstreetcode.teammate.model.Event}
 */
public class MapInputViewHolder extends InputViewHolder<EventEditAdapter.EventEditAdapterListener>
        implements View.OnClickListener {

    private final MapView mapView;
    private final Button addButton;

    public MapInputViewHolder(View itemView, EventEditAdapter.EventEditAdapterListener listener) {
        super(itemView, listener::canEditEvent);
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
            mapView.getMapAsync(map -> {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(location);
                map.moveCamera(cameraUpdate);
                map.clear();
                map.addMarker(new MarkerOptions().position(location));
                map.getUiSettings().setMapToolbarEnabled(false);
            });
        }
    }

    @Override
    public void onClick(View view) {
        if (isEnabled()) adapterListener.onLocationClicked();
    }

}
