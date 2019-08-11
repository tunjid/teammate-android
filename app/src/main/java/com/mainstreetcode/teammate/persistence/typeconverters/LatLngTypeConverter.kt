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

package com.mainstreetcode.teammate.persistence.typeconverters

import androidx.room.TypeConverter
import android.text.TextUtils

import com.google.android.gms.maps.model.LatLng


class LatLngTypeConverter {

    @TypeConverter
    fun toDbValue(latLng: LatLng?): String {
        return if (latLng == null) "" else latLng.latitude.toString() + "," + latLng.longitude
    }

    @TypeConverter
    fun fromId(source: String): LatLng? {
        if (TextUtils.isEmpty(source)) return null
        val split = source.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val latitude = java.lang.Double.parseDouble(split[0])
        val longitude = java.lang.Double.parseDouble(split[1])
        return LatLng(latitude, longitude)
    }
}
