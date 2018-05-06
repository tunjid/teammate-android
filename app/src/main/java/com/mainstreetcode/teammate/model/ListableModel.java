package com.mainstreetcode.teammate.model;

import java.util.ArrayList;
import java.util.List;

public interface ListableModel<T> {

    List<Item<T>> asItems();

    default List<Identifiable> asIdentifiables() {return new ArrayList<>(asItems());}
}
