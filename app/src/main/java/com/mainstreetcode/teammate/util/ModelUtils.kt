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
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
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

fun <K, V> get(key: K, getter: (K) -> V?, setter: (K, V) -> Unit, instantiator: () -> V): V =
        getter.invoke(key) ?: {
            val temp = instantiator.invoke()
            setter.invoke(key, temp)
            temp
        }()

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

