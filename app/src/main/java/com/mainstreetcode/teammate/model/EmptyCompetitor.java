package com.mainstreetcode.teammate.model;

import android.os.Parcel;

public class EmptyCompetitor implements Competitive {
    public EmptyCompetitor() {}

    private EmptyCompetitor(Parcel in) {in.readString();}

    public String getId() { return ""; }

    public String getRefType() { return ""; }

    public String getImageUrl() { return ""; }

    public CharSequence getName() { return ""; }

    @Override
    public boolean isEmpty() { return true; }

    public boolean hasMajorFields() { return false; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {dest.writeString(""); }

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
