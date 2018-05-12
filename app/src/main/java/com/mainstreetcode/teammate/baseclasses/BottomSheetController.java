package com.mainstreetcode.teammate.baseclasses;


import android.support.annotation.MenuRes;

import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

public interface BottomSheetController {
    void hideBottomSheet();

    void showBottomSheet(Args args);

    class Args {

        @MenuRes
        private final int menuRes;
        private final String title;
        private final BaseFragment fragment;

        private Args(int menuRes, String title, BaseFragment fragment) {
            this.menuRes = menuRes;
            this.title = title;
            this.fragment = fragment;
        }

        public static Builder builder() {
            return new Builder();
        }

        public int getMenuRes() {
            return menuRes;
        }

        public String getTitle() {
            return title;
        }

        public BaseFragment getFragment() {
            return fragment;
        }

        public static class Builder {
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
}
