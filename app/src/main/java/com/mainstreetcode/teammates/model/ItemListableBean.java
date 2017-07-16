package com.mainstreetcode.teammates.model;

import com.mainstreetcode.teammates.util.ListableBean;

import java.util.List;

/**
 * Class that hosts a
 */

public interface  ItemListableBean<T> extends ListableBean<Item> {
     List<Item<T>> buildItems();
}
