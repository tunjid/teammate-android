package com.mainstreetcode.teammate.model;

import android.os.Parcelable;

public interface Competitive extends RemoteImage, Parcelable {

    String getId();

    String getRefType();

    CharSequence getName();

    Competitive makeCopy();

    boolean isEmpty();

    boolean hasMajorFields();

    default boolean update(Competitive other) { return false; }
}