package com.mainstreetcode.teammates.model;


import android.os.Parcelable;

/**
 * Base interface for model interactions
 */
public interface Model<T> extends Parcelable {
    void update(T updated);

    boolean isEmpty();

    String getId();

    String getImageUrl();
}
