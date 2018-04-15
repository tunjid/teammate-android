package com.mainstreetcode.teammate.model;

import com.mainstreetcode.teammate.util.ListableBean;

import java.util.List;

/**
 * Class that hosts a
 */

public interface ItemListableBean<T> extends ListableBean<Item> {
    List<Item<T>> buildItems();
}
