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

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

private val fullPrinter = SimpleDateFormat("MMM, d yyyy", Locale.US)

private val prettyPrinter = SimpleDateFormat("EEE, d MMM yyyy HH:mm", Locale.US)

private val dateFormatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

fun Date.ISO8601Print(): String = dateFormatter.format(this)

fun Date.prettyPrint(): String = prettyPrinter.format(this)

fun Date.calendarPrint(): String = fullPrinter.format(this)

fun String.asIntOrZero(): Int = operateOn(0, { toInt() })

fun String.asFloatOrZero(): Float = operateOn(0F, { toFloat() })

fun String.asBooleanOrFalse(): Boolean = operateOn(false, { toBoolean() })

fun String.parseISO8601Date(): Date = synchronized(dateFormatter) {
    return parseDate(this, dateFormatter)
}

fun String.parsePrettyDate(): Date = synchronized(prettyPrinter) {
    return parseDate(this, prettyPrinter)
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

private fun parseDate(date: String, formatter: SimpleDateFormat): Date {
    if (date.isBlank()) return Date()
    return try {
        formatter.parse(date) ?: Date()
    } catch (e: ParseException) {
        Date()
    }
}

private inline fun <T> String.operateOn(default: T, function: (String) -> T): T {
    if (isBlank()) return default

    try {
        return function.invoke(this)
    } catch (e: Exception) {
        Logger.log("ModelUtils", "Number Format Exception", e)
    }

    return default
}
