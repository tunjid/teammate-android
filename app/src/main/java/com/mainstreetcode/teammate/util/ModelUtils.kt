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

import android.location.Address
import androidx.emoji.text.EmojiCompat
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.tunjid.androidbootstrap.functions.collections.Lists
import com.tunjid.androidbootstrap.functions.collections.Lists.transform
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * Static methods for models
 */

const val EMPTY_STRING = ""

val dateFormatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

private val fullPrinter = SimpleDateFormat("MMM, d yyyy", Locale.US)
val prettyPrinter = SimpleDateFormat("EEE, d MMM yyyy HH:mm", Locale.US)


private val screenName = Pattern.compile("[^a-z0-9_]", Pattern.CASE_INSENSITIVE)

fun CharSequence.isValidScreenName(): Boolean = !screenName.matcher(this).find()

fun Date.prettyPrint(): String = prettyPrinter.format(this)
fun Date.calendarPrint(): String = fullPrinter.format(this)

fun <T> deserializeList(context: JsonDeserializationContext, listElement: JsonElement?,
                        destination: MutableList<T>, type: Class<T>) {
    if (listElement != null && listElement.isJsonArray) {
        val jsonArray = listElement.asJsonArray

        for (element in jsonArray) {
            destination.add(context.deserialize(element, type))
        }
    }
}

fun <K, V> get(key: K, getter: (K) -> V?, setter: (K, V) -> Unit, instantiator: () -> V): V =
        getter.invoke(key) ?: {
            val temp = instantiator.invoke()
            setter.invoke(key, temp)
            temp
        }()

fun asDifferentiables(subTypeList: List<Differentiable>): List<Differentiable> =
        ArrayList(subTypeList)

fun replaceStringList(sourceList: List<String>, updatedList: List<String>) {
    val source = transform<String, Differentiable>(sourceList, { s -> Differentiable.fromCharSequence { s } }, { it.id })
    val updated = transform<String, Differentiable>(updatedList, { s -> Differentiable.fromCharSequence { s } }, { it.id })
    replaceList(source, updated)
}

fun <T : Differentiable> preserveAscending(source: MutableList<T>, additions: List<T>) {
    concatenateList(source, additions)
    Collections.sort(source, FunctionalDiff.COMPARATOR)
}

fun <T : Differentiable> preserveDescending(source: MutableList<T>, additions: List<T>) {
    concatenateList(source, additions)
    Collections.sort(source, FunctionalDiff.DESCENDING_COMPARATOR)
}

fun <T : Differentiable> replaceList(source: List<T>, additions: List<T>): List<T> {
    Lists.replace(source, additions)
    Collections.sort(source, FunctionalDiff.COMPARATOR)
    return source
}

private fun <T : Differentiable> concatenateList(source: MutableList<T>, additions: List<T>) {
    val set = HashSet(additions)
    set.addAll(source)
    source.clear()
    source.addAll(set)
}

fun CharSequence.processEmoji(): CharSequence {
    val emojiCompat = EmojiCompat.get()
    return if (emojiCompat.loadState == EmojiCompat.LOAD_STATE_SUCCEEDED) emojiCompat.process(this) else this
}

fun areDifferentDays(prev: Date?, next: Date): Boolean {
    if (prev == null) return false

    val prevCal = Calendar.getInstance()
    val nextCal = Calendar.getInstance()
    prevCal.time = prev
    nextCal.time = next

    return (prevCal.get(Calendar.DAY_OF_MONTH) != nextCal.get(Calendar.DAY_OF_MONTH)
            || prevCal.get(Calendar.MONTH) != nextCal.get(Calendar.MONTH)
            || prevCal.get(Calendar.YEAR) != nextCal.get(Calendar.YEAR))
}

val Address.fullName: String
    get() {
        val addressBuilder = StringBuilder()
        val end = maxAddressLineIndex

        for (i in 0..end) {
            addressBuilder.append(getAddressLine(i))
            if (i != end) addressBuilder.append("\n")
        }

        return addressBuilder.toString()
    }

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

fun areNotEmpty(vararg values: CharSequence): Boolean {
    for (value in values) if (value.isBlank()) return false
    return true
}

fun String.parseDateISO8601(): Date {
    val result: Date
    synchronized(dateFormatter) {
        result = parseDate(this, dateFormatter)
    }
    return result
}

fun parseDate(date: String, formatter: SimpleDateFormat): Date {
    if (date.isBlank()) return Date()
    return try {
        formatter.parse(date) ?: Date()
    } catch (e: ParseException) {
        Date()
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

fun String.asIntOrFalse(): Int {
    if (isBlank()) return 0
    try {
        return Integer.valueOf(this)
    } catch (e: Exception) {
        Logger.log("ModelUtils", "Number Format Exception", e)
    }

    return 0
}

fun String.asFloatOrFalse(): Float {
    if (isBlank()) return 0F
    try {
        return java.lang.Float.valueOf(this)
    } catch (e: Exception) {
        Logger.log("ModelUtils", "Number Format Exception", e)
    }

    return 0F
}

fun String.asBooleanOrFalse(): Boolean {
    if (isBlank()) return false
    try {
        return java.lang.Boolean.valueOf(this)
    } catch (e: Exception) {
        Logger.log("ModelUtils", "Number Format Exception", e)
    }

    return false
}