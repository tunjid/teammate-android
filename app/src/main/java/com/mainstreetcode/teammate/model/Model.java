package com.mainstreetcode.teammate.model;


import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Base interface for model interactions
 */
public interface Model<T> extends Identifiable, Parcelable, Comparable<T> {
    /**
     * Provides a means to reset a local copy of the model before being updated with values from the remote.
     */
    void reset();

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

    String getImageUrl();

    default boolean hasMajorFields() {
        return !TextUtils.isEmpty(getImageUrl());
    }
}
