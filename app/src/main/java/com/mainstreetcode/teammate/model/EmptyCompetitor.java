package com.mainstreetcode.teammate.model;

import android.os.Parcel;

public class EmptyCompetitor implements Competitive {
    public EmptyCompetitor() {}

    @SuppressWarnings("unused")
    private EmptyCompetitor(Parcel in) {}

    public String getId() { return ""; }

    public String getRefType() { return ""; }

    public String getImageUrl() { return ""; }

    public CharSequence getName() { return ""; }

    public boolean hasMajorFields() { return false; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) { }

    public static final Creator<EmptyCompetitor> CREATOR = new Creator<EmptyCompetitor>() {
        @Override
        public EmptyCompetitor createFromParcel(Parcel in) {
            return new EmptyCompetitor(in);
        }

        @Override
        public EmptyCompetitor[] newArray(int size) {
            return new EmptyCompetitor[size];
        }
    };
}
