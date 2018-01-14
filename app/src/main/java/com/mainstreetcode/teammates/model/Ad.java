package com.mainstreetcode.teammates.model;


import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.mainstreetcode.teammates.util.ObjectId;

import java.util.List;

public class Ad<T extends NativeAd> implements Model {

    private T nativeAd;
    private final String id = new ObjectId().toHexString();

    public Ad(T nativeAd) {
        this.nativeAd = nativeAd;
    }

    @Nullable
    public String getImageAspectRatio() {
        NativeAd.Image image = getImage();
        if (image == null) return null;

        Drawable drawable = image.getDrawable();
        if (drawable == null) return null;

        return "H," + drawable.getIntrinsicWidth() + ":" + drawable.getIntrinsicHeight();
    }

    @Nullable
    public Drawable getDrawable() {
        NativeAd.Image image = getImage();
        if (image == null) return null;

        return image.getDrawable();
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
        NativeAd.Image image = getImage();
        return image == null ? "" : image.getUri().toString();
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

    @Nullable
    private NativeAd.Image getImage() {
        List<NativeAd.Image> images = null;
        if (nativeAd instanceof NativeContentAd) images = ((NativeContentAd) nativeAd).getImages();
        else if (nativeAd instanceof NativeAppInstallAd)
            images = ((NativeAppInstallAd) nativeAd).getImages();

        if (images == null || images.isEmpty()) return null;
        return images.get(0);
    }
}
