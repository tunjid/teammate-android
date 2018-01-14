package com.mainstreetcode.teammates.model;


import android.os.Parcel;
import android.support.annotation.NonNull;

import com.google.android.gms.ads.formats.NativeAd;
import com.mainstreetcode.teammates.util.ObjectId;

public class Ad<T extends NativeAd> implements Model {

    private T nativeAd;
    private final String id = new ObjectId().toHexString();

    public Ad(T nativeAd) {
        this.nativeAd = nativeAd;
    }

    @Override
    public String getId() {
        return id;
    }

    public T getNativeAd() {
        return nativeAd;
    }

    @Override
    public void reset() {

    }

    @Override
    public void update(Object updated) {

    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public String getImageUrl() {
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    @Override
    public int compareTo(@NonNull Object o) {
        return 0;
    }
}
