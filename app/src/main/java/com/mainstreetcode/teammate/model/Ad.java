package com.mainstreetcode.teammate.model;


import com.mainstreetcode.teammate.util.ObjectId;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.util.List;

import androidx.annotation.Nullable;

public abstract class Ad<T> implements Differentiable {

    private T nativeAd;
    private final String id = new ObjectId().toHexString();

    //    Ad(T nativeAd) {
//        this.nativeAd = nativeAd;
//    }
//
    public abstract int getType();

    @Override
    public String getId() {
        return id;
    }

    //
//    public T getNativeAd() {
//        return nativeAd;
//    }
//
    @Nullable
    public String getImageAspectRatio() {
        return "H,1:1";
//        NativeAd.Image image = getImage();
//        if (image == null) return null;
//
//        Drawable drawable = image.getDrawable();
//        if (drawable == null) return null;
//
//        return "H," + drawable.getIntrinsicWidth() + ":" + drawable.getIntrinsicHeight();
    }
//
//    @Nullable
//    public NativeAd.Image getImage() {
//        List<NativeAd.Image> images = null;
//        if (nativeAd instanceof NativeContentAd) images = ((NativeContentAd) nativeAd).getImages();
//        else if (nativeAd instanceof NativeAppInstallAd)
//            images = ((NativeAppInstallAd) nativeAd).getImages();
//
//        if (images == null || images.isEmpty()) return null;
//        return images.get(0);
//    }
}
