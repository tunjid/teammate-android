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
    @IntDef({INPUT, IMAGE, ROLE})
    @interface ItemType {}

    public static final int INPUT = 2;
    public static final int IMAGE = 3;
    public static final int ROLE = 4;

    private @ItemType final int itemType;
    private @StringRes final int stringRes;
    private @StringRes final int headerStringRes;
    private @Nullable final ValueChangeCallBack changeCallBack;
    private final Class<T> itemizedClass;

    String value;

    Item(int itemType, int stringRes, String value, @Nullable ValueChangeCallBack changeCallBack,
         Class<T> itemizedClass) {
        this(itemType, stringRes, 0, value, changeCallBack, itemizedClass);
    }

    Item(int itemType, int stringRes, int headerStringRes,
         String value, @Nullable ValueChangeCallBack changeCallBack, Class<T> itemizedClass) {
        this.itemType = itemType;
        this.stringRes = stringRes;
        this.headerStringRes = headerStringRes;
        this.value = value;
        this.changeCallBack = changeCallBack;
        this.itemizedClass = itemizedClass;
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

    public Class<T> getItemizedClass() {
        return itemizedClass;
    }

    // Used to change the value of the Team's fields
    interface ValueChangeCallBack {
        void onValueChanged(String value);
    }
}
