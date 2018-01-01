package com.mainstreetcode.teammates.model;


import android.os.Parcelable;

import java.util.Comparator;

import static com.mainstreetcode.teammates.model.Model.Util.getPoints;

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

    @SuppressWarnings("unchecked")
    Comparator<Model> COMPARATOR = (modelA, modelB) -> {
        int pointsA = getPoints(modelA);
        int pointsB = getPoints(modelB);

        int a, b;
        a = b = Integer.compare(pointsA, pointsB);
        if (modelA.getClass().equals(modelB.getClass())) a += modelA.compareTo(modelB);

        return Integer.compare(a, b);
    };

    class Util {
        static int getPoints(Model model) {
            if (model.getClass().equals(Role.class)) return 20;
            if (model.getClass().equals(JoinRequest.class)) return 15;
            if (model.getClass().equals(Event.class)) return 10;
            if (model.getClass().equals(Media.class)) return 5;
            return 0;
        }
    }
}
