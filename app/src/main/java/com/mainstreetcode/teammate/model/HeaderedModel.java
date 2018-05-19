package com.mainstreetcode.teammate.model;

public interface HeaderedModel<T> extends Model<T> {
    Item<T> getHeaderItem();
}
