package com.mainstreetcode.teammates.model;



public interface Model<T> {
    boolean isEmpty();
    String getId();
    String getImageUrl();
    void update(T updated);
}
