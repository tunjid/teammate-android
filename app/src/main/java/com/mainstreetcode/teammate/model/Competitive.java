package com.mainstreetcode.teammate.model;

import android.os.Parcelable;

public interface Competitive extends Parcelable{

    String getId();

    String getRefType();

    String getImageUrl();

    CharSequence getName();

    boolean hasMajorFields();

}