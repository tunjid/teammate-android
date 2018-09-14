package com.mainstreetcode.teammate.model;

import android.os.Parcelable;

public interface Competitive extends RemoteImage, Parcelable {

    String getId();

    String getRefType();

    CharSequence getName();

    boolean isEmpty();

    boolean hasMajorFields();
}