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

package com.mainstreetcode.teammate.util

import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonObject

fun JsonObject.asBooleanOrFalse(key: String): Boolean {
    val element = get(key)
    return try {
        element != null && element.isJsonPrimitive && element.asBoolean
    } catch (e: Exception) {
        false
    }
}

fun JsonObject.asStringOrEmpty(key: String): String {
    val element = get(key)
    return try {
        if (element != null && element.isJsonPrimitive) element.asString else ""
    } catch (e: Exception) {
        ""
    }
}

fun JsonObject.asFloatOrZero(key: String): Float {
    val element = get(key)
    return try {
        if (element != null && element.isJsonPrimitive) element.asFloat else 0F
    } catch (e: Exception) {
        0f
    }
}

fun JsonElement.parseCoordinates(key: String): LatLng? {
    if (!isJsonObject) return null

    val element = asJsonObject.get(key)
    if (element == null || !element.isJsonArray) return null

    val array = element.asJsonArray
    if (array.size() != 2) return null

    val longitude = array.get(0)
    val latitude = array.get(1)

    if (!longitude.isJsonPrimitive || !latitude.isJsonPrimitive) return null

    return try {
        LatLng(latitude.asDouble, longitude.asDouble)
    } catch (e: Exception) {
        null
    }

}

fun <T> JsonDeserializationContext.deserializeList(listElement: JsonElement?, destination: MutableList<T>, type: Class<T>) {
    if (listElement != null && listElement.isJsonArray) for (element in listElement.asJsonArray) destination.add(deserialize(element, type))
}