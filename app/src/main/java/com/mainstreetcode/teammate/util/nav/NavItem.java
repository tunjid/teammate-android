package com.mainstreetcode.teammate.util.nav;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.StringRes;

public class NavItem implements Parcelable{
    @IdRes final int idRes;
    @StringRes final int titleRes;
    @DrawableRes final int drawableRes;

    private NavItem(int idRes, int titleRes, int drawableRes) {
        this.idRes = idRes;
        this.titleRes = titleRes;
        this.drawableRes = drawableRes;
    }

    private NavItem(Parcel in) {
        idRes = in.readInt();
        titleRes = in.readInt();
        drawableRes = in.readInt();
    }

    public static NavItem create(@IdRes int idRes, @StringRes int titleRes, @DrawableRes int drawableRes) {
        return new NavItem(idRes, titleRes, drawableRes);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(idRes);
        dest.writeInt(titleRes);
        dest.writeInt(drawableRes);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<NavItem> CREATOR = new Parcelable.Creator<NavItem>() {
        @Override
        public NavItem createFromParcel(Parcel in) {
            return new NavItem(in);
        }

        @Override
        public NavItem[] newArray(int size) {
            return new NavItem[size];
        }
    };
}
