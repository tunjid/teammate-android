package com.mainstreetcode.teammate.persistence.typeconverters;

import android.arch.persistence.room.TypeConverter;

import com.mainstreetcode.teammate.util.ModelUtils;


public class CharSequenceConverter {
    @TypeConverter
    public CharSequence formString(String value) {
        return value == null ? null : ModelUtils.processString(value);
    }

    @TypeConverter
    public String toString(CharSequence sequence) {
        return sequence == null ? null : sequence.toString();
    }
}
