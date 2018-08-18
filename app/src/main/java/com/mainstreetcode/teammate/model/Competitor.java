package com.mainstreetcode.teammate.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.mainstreetcode.teammate.util.IdCache;

public interface Competitor<T> extends Model<T> {

    CharSequence getName();

    String getType();

    default boolean hasSameType(Competitor other) {
        return getType().equals(other.getType());
    }

    class Util {

        public static Competitor empty() {
            return new Empty();
        }

        public static Competitor deserialize(String refPath, @Nullable JsonElement element, JsonDeserializationContext context) {
            boolean hasElement = element != null;

            switch (refPath) {
                case User.COMPETITOR_TYPE:
                    return hasElement ? context.deserialize(element, User.class) : User.empty();
                case Team.COMPETITOR_TYPE:
                    return hasElement ? context.deserialize(element, Team.class) : Team.empty();
                default:
                    return empty();
            }
        }

        public static Competitor fromParcel(Parcel in) {
            String refPath = in.readString();
            switch (refPath) {
                case User.COMPETITOR_TYPE:
                    return (User) in.readValue(User.class.getClassLoader());

                case Team.COMPETITOR_TYPE:
                    return (Team) in.readValue(Team.class.getClassLoader());
                default:
                    return empty();
            }
        }

        public static void writeToParcel(Competitor competitor, Parcel dest) {
            String refPath = competitor.getType();
            dest.writeString(refPath);
            switch (refPath) {
                case User.COMPETITOR_TYPE:
                case Team.COMPETITOR_TYPE:
                    dest.writeValue(competitor);
                    break;
            }
        }
    }

    class Empty implements Competitor, Parcelable {

        private final String id = IdCache.cache(1).get(0);

        private Empty() {}

        protected Empty(Parcel in) { }

        @Override
        public CharSequence getName() { return ""; }

        @Override
        public String getImageUrl() { return ""; }

        @Override
        public String getType() { return "empty"; }

        @Override
        public boolean isEmpty() { return true; }

        @Override
        public void update(Object updated) { }

        @Override
        public String getId() { return id; }

        @Override
        public int compareTo(@NonNull Object o) {
            return 0;
        }

        @Override
        public int describeContents() { return 0; }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(getType());
        }

        public static final Creator<Empty> CREATOR = new Creator<Empty>() {
            @Override
            public Empty createFromParcel(Parcel in) {
                return new Empty(in);
            }

            @Override
            public Empty[] newArray(int size) {
                return new Empty[size];
            }
        };
    }
}
