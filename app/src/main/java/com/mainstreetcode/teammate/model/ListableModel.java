package com.mainstreetcode.teammate.model;

import java.util.ArrayList;
import java.util.List;

public interface ListableModel<T> {

    List<Item<T>> asItems();

    default List<Identifiable> asIdentifiables() {return new ArrayList<>(asItems());}

    default void restItemList() {
        List<Item<T>> items = asItems();

        int size = items.size();
        for (int i = 0; i < size; i++) items.get(i).setValue("");
    }

    default void updateItemList(ListableModel<T> other) {
        List<Item<T>> items = asItems();
        List<Item<T>> otherItems = other.asItems();

        int size = items.size();
        for (int i = 0; i < size; i++) items.get(i).setValue(otherItems.get(i).getValue());
    }
}
