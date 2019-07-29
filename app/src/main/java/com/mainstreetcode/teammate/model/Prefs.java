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


import android.annotation.SuppressLint;
import android.os.Parcel;
import androidx.annotation.NonNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

@SuppressLint("ParcelCreator")
public class Prefs implements Model<Prefs> {

    public static Prefs empty() {return new Prefs();}

    private boolean onBoarded;

    private Prefs() {}

    public boolean isOnBoarded() { return onBoarded; }

    public void setOnBoarded(boolean onBoarded) { this.onBoarded = onBoarded; }

    @Override
    public void update(Prefs updated) {
        this.onBoarded = updated.onBoarded;
    }

    @Override
    public int compareTo(@NonNull Prefs o) {
        return 0;
    }

    @Override
    public boolean isEmpty() { return false; }

    @Override
    public String getId() {
        return "1";
    }

    @Override
    public String getImageUrl() {
        return "";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) { }

    public static class GsonAdapter
            implements
            JsonSerializer<Prefs> {

        private static final String ON_BOARDED = "onBoarded";

        @Override
        public JsonElement serialize(Prefs src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject serialized = new JsonObject();

            serialized.addProperty(ON_BOARDED, src.onBoarded);

            return serialized;
        }
    }
}
