/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.model;

import android.os.Parcel;

public class EmptyCompetitor implements Competitive {
    public EmptyCompetitor() {}

    private EmptyCompetitor(Parcel in) {in.readString();}

    public String getId() { return ""; }

    public String getRefType() { return ""; }

    public String getImageUrl() { return Config.getDefaultTeamLogo(); }

    public CharSequence getName() { return ""; }

    @Override
    public Competitive makeCopy() { return new EmptyCompetitor(); }

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
