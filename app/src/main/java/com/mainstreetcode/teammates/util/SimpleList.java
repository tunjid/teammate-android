package com.mainstreetcode.teammates.util;

/**
 * A Simple List of items
 * <p>
 * Created by Shemanigans on 6/4/17.
 */

public interface SimpleList<T> {
    T get(int position);
    int size();
}
