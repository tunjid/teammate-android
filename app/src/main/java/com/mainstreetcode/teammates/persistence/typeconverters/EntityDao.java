package com.mainstreetcode.teammates.persistence.typeconverters;

import java.util.List;


public abstract class EntityDao<T> {

    protected abstract void insert(List<T> models);

    protected abstract void update(List<T> models);

    public void upsert(List<T> models) {
        insert(models);
        update(models);
    }
}
