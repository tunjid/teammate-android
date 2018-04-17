package com.mainstreetcode.teammate.model;

import android.arch.core.util.Function;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.InputType;

import com.mainstreetcode.teammate.util.ObjectId;
import com.mainstreetcode.teammate.util.Supplier;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Item for listing properties of a {@link Model}
 */
public class Item<T> implements Identifiable, Comparable<Item> {

    @Retention(SOURCE)
    @IntDef({INPUT, IMAGE, ROLE, DATE, CITY, LOCATION, INFO, TEXT, NUMBER, SPORT, VISIBILITY})
    @interface ItemType {}

    public static final int INPUT = 2;
    public static final int IMAGE = 3;
    public static final int ROLE = 4;
    public static final int DATE = 5;

    public static final int CITY = 6;
    public static final int ZIP = 8;
    public static final int STATE = 7;
    public static final int SPORT = 13;

    public static final int LOCATION = 9;

    public static final int INFO = 10;
    public static final int TEXT = 11;
    public static final int NUMBER = 12;
    public static final int DESCRIPTION = 14;
    public static final int VISIBILITY = 15;

    public static final Supplier<Boolean> TRUE = () -> true;
    public static final Supplier<Boolean> FALSE = () -> false;

    private final int sortPosition;
    private final int inputType;
    private @ItemType final int itemType;
    private @StringRes final int stringRes;
    private @Nullable final ValueChangeCallBack changeCallBack;
    private @Nullable Function<CharSequence, CharSequence> textTransformer;

    private final T itemizedObject;
    private final String id = new ObjectId().toHexString();

    private CharSequence value;

    Item(int sortPosition, int inputType, int itemType, int stringRes,
         CharSequence value, @Nullable ValueChangeCallBack changeCallBack, T itemizedObject) {
        this.sortPosition = sortPosition;
        this.inputType = inputType;
        this.itemType = itemType;
        this.stringRes = stringRes;
        this.value = value;
        this.changeCallBack = changeCallBack;
        this.itemizedObject = itemizedObject;
    }


    public static <T> Item<T> number(int sortPosition, int itemType, int stringRes, Supplier<CharSequence> supplier, @Nullable ValueChangeCallBack changeCallBack,
                                     T itemizedObject) {
        return new Item<>(sortPosition, InputType.TYPE_CLASS_NUMBER, itemType, stringRes, supplier.get(), changeCallBack, itemizedObject);
    }

    public static <T> Item<T> text(int sortPosition, int itemType, int stringRes, Supplier<CharSequence> supplier, @Nullable ValueChangeCallBack changeCallBack,
                                   T itemizedObject) {
        return new Item<>(sortPosition, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, itemType, stringRes, supplier.get(), changeCallBack, itemizedObject);
    }

    public static <T> Item<T> email(int sortPosition, int itemType, int stringRes, Supplier<CharSequence> supplier, @Nullable ValueChangeCallBack changeCallBack,
                                    T itemizedObject) {
        return new Item<>(sortPosition, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, itemType, stringRes, supplier.get(), changeCallBack, itemizedObject);
    }

    public static Supplier<CharSequence> nullToEmpty(@Nullable String source) {
        CharSequence finalSource = source == null ? "" : source;
        return () -> finalSource;
    }

    public void setValue(CharSequence value) {
        this.value = value;
        if (changeCallBack != null) changeCallBack.onValueChanged(value.toString());
    }

    public Item textTransformer(Function<CharSequence, CharSequence> textTransformer) {
        this.textTransformer = textTransformer;
        return this;
    }

    public int getInputType() { return inputType; }

    public int getItemType() {return this.itemType;}

    public int getStringRes() {return this.stringRes;}

    public CharSequence getValue() {return textTransformer == null ? value : textTransformer.apply(value);}

    @Override
    public String getId() {return id;}

    @Override
    public int compareTo(@NonNull Item o) { return Integer.compare(sortPosition, o.sortPosition); }

    @Override
    public boolean areContentsTheSame(Identifiable other) {
        if (other instanceof Item) return value.equals(((Item) other).value);
        return id.equals(other.getId());
    }

    @Override
    public Object getChangePayload(Identifiable other) {
        return other;
    }

    public T getItemizedObject() {
        return itemizedObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;

        Item<?> item = (Item<?>) o;

        return value != null ? value.equals(item.value) : item.value == null;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    // Used to change the value of the Team's fields
    public interface ValueChangeCallBack {
        void onValueChanged(String value);
    }


}
