package com.mainstreetcode.teammates.model;


import com.mainstreetcode.teammates.repository.CrudRespository;

/**
 * Interface definition to work around generic bounds in {@link BaseModel}
 */
public interface Model<T extends BaseModel<T>> extends BaseModel<T> {

    CrudRespository<T> getRepository();
}
