package com.mainstreetcode.teammate.model;

public class Builder<T> {
    private int sortPosition;
    private int inputType;
    private int itemType;
    private int stringRes;
    private CharSequence value;
    private Item.ValueChangeCallBack changeCallBack;
    private T itemizedObject;

    public Builder<T> setSortPosition(int sortPosition) {
        this.sortPosition = sortPosition;
        return this;
    }

    public Builder<T> setInputType(int inputType) {
        this.inputType = inputType;
        return this;
    }

    public Builder<T> setItemType(int itemType) {
        this.itemType = itemType;
        return this;
    }

    public Builder<T> setStringRes(int stringRes) {
        this.stringRes = stringRes;
        return this;
    }

    public Builder<T> setValue(CharSequence value) {
        this.value = value;
        return this;
    }

    public Builder<T> setChangeCallBack(Item.ValueChangeCallBack changeCallBack) {
        this.changeCallBack = changeCallBack;
        return this;
    }

    public Builder<T> setItemizedObject(T itemizedObject) {
        this.itemizedObject = itemizedObject;
        return this;
    }

    public Item<T> createItem() {
        return new Item<>(sortPosition, inputType, itemType, stringRes, value, changeCallBack, itemizedObject);
    }
}