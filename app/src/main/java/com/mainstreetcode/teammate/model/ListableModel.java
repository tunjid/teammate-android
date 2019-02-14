package com.mainstreetcode.teammate.model;

import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.util.ArrayList;
import java.util.List;

public interface ListableModel<T> {

    List<Item<T>> asItems();

    default List<Differentiable> asDifferentiables() {return new ArrayList<>(asItems());}
}
