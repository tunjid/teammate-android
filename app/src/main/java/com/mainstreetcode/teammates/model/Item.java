package com.mainstreetcode.teammates.model;

import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Item for listing properties of a {@link com.mainstreetcode.teammates.util.ListableBean}
 * <p>
 * Created by Shemanigans on 6/18/17.
 */
public class Item<T> {

    @Retention(SOURCE)
    @IntDef({INPUT, IMAGE, ROLE, DATE, ADDRESS, LOCATION})
    @interface ItemType {}

    public static final int INPUT = 2;
    public static final int IMAGE = 3;
    public static final int ROLE = 4;
    public static final int DATE = 5;
    public static final int ADDRESS = 6;
    public static final int LOCATION = 7;

    private @ItemType final int itemType;
    private @StringRes final int stringRes;
    private @StringRes final int headerStringRes;
    private @Nullable final ValueChangeCallBack changeCallBack;
    private final T itemizedObject;

    private String value;

    public Item(int itemType, int stringRes, String value, @Nullable ValueChangeCallBack changeCallBack,
                 T itemizedObject) {
        this(itemType, stringRes, 0, value, changeCallBack, itemizedObject);
    }

    public Item(int itemType, int stringRes, int headerStringRes,
                String value, @Nullable ValueChangeCallBack changeCallBack, T itemizedObject) {
        this.itemType = itemType;
        this.stringRes = stringRes;
        this.headerStringRes = headerStringRes;
        this.value = value;
        this.changeCallBack = changeCallBack;
        this.itemizedObject = itemizedObject;
    }

    public void setValue(String value) {
        this.value = value;
        if (changeCallBack != null) changeCallBack.onValueChanged(value);
    }

    public int getItemType() {return this.itemType;}

    public int getStringRes() {return this.stringRes;}

    public int getHeaderStringRes() {
        return headerStringRes;
    }

    public String getValue() {return this.value;}

    public T getItemizedObject() {
        return itemizedObject;
    }

    // Used to change the value of the Team's fields
    public interface ValueChangeCallBack {
        void onValueChanged(String value);
    }
}
