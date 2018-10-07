package com.mainstreetcode.teammate.baseclasses;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.MenuRes;

import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

public interface BottomSheetController {

    void hideBottomSheet();

    void showBottomSheet(Args args);

    boolean isBottomSheetShowing();

    class ToolbarState implements Parcelable {
        @MenuRes
        private final int menuRes;
        private final String title;

        ToolbarState(int menuRes, String title) {
            this.menuRes = menuRes;
            this.title = title;
        }

        private ToolbarState(Parcel in) {
            menuRes = in.readInt();
            title = in.readString();
        }

        public int getMenuRes() {
            return menuRes;
        }

        public String getTitle() {
            return title;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(menuRes);
            dest.writeString(title);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<ToolbarState> CREATOR = new Parcelable.Creator<ToolbarState>() {
            @Override
            public ToolbarState createFromParcel(Parcel in) {
                return new ToolbarState(in);
            }

            @Override
            public ToolbarState[] newArray(int size) {
                return new ToolbarState[size];
            }
        };
    }

    class Args {
        private final ToolbarState toolbarState;
        private final BaseFragment fragment;

        private Args(int menuRes, String title, BaseFragment fragment) {
            toolbarState = new ToolbarState(menuRes, title);
            this.fragment = fragment;
        }

        public static Builder builder() {
            return new Builder();
        }

        public ToolbarState getToolbarState() {
            return toolbarState;
        }

        public BaseFragment getFragment() {
            return fragment;
        }
    }

    class Builder {
        private int menuRes;
        private String title;
        private BaseFragment fragment;

        public Builder setMenuRes(int menuRes) {
            this.menuRes = menuRes;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setFragment(BaseFragment fragment) {
            this.fragment = fragment;
            return this;
        }

        public Args build() {
            return new Args(menuRes, title, fragment);
        }
    }
}
