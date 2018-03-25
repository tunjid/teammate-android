package com.mainstreetcode.teammate.util;

/**
 * A Simple List of items
 * <p>
 * Created by Shemanigans on 6/4/17.
 */

public interface ListableBean<Item> {
    int size();

    Item get(int position);
}
