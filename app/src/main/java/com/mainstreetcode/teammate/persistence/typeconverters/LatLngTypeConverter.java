package com.mainstreetcode.teammate.persistence.typeconverters;

import androidx.room.TypeConverter;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;


public class LatLngTypeConverter {

    @TypeConverter
    public String toDbValue(LatLng latLng) {
        return latLng == null ? "" : latLng.latitude + "," + latLng.longitude;
    }

    @TypeConverter
    public LatLng fromId(String source) {
        if (TextUtils.isEmpty(source)) return null;
        String[] split = source.split(",");
        double latitude = Double.parseDouble(split[0]);
        double longitude = Double.parseDouble(split[1]);
        return new LatLng(latitude, longitude);
    }
}
