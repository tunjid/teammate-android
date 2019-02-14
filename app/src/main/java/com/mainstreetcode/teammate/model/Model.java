package com.mainstreetcode.teammate.model;


import android.os.Parcelable;
import android.text.TextUtils;

import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

/**
 * Base interface for model interactions
 */
public interface Model<T> extends RemoteImage, Differentiable, Parcelable, Comparable<T> {
    /**
     * Update the current model with values in the model provided, while keeping values in
     * data structures like {@link java.util.List lists}
     *
     * @param updated the model providing new values
     */
    void update(T updated);

    /**
     * @return whether this object was created locally or exists in the remote.
     */
    boolean isEmpty();

    default boolean hasMajorFields() {
        return !TextUtils.isEmpty(getImageUrl());
    }
}
