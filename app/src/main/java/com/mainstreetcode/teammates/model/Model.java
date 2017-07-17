package com.mainstreetcode.teammates.model;



public interface Model<T> {
    boolean isEmpty();
    void update(T updated);
}
