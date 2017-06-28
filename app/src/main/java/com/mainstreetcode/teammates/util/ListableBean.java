package com.mainstreetcode.teammates.util;

/**
 * A Simple List of items
 * <p>
 * Created by Shemanigans on 6/4/17.
 */

public interface ListableBean<Source, Item> {
    int size();

    Item get(int position);

    Source toSource();
}
