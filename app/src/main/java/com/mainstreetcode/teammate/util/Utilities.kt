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
import java.util.regex.Pattern

/**
 * Static methods for models
 */

const val EMPTY_STRING = ""

private val screenName = Pattern.compile("[^a-z0-9_]", Pattern.CASE_INSENSITIVE)

fun CharSequence.isValidScreenName(): Boolean = !screenName.matcher(this).find()

fun CharSequence.processEmoji(): CharSequence {
    val emojiCompat = EmojiCompat.get()
    return if (emojiCompat.loadState == EmojiCompat.LOAD_STATE_SUCCEEDED) emojiCompat.process(this) else this
}

fun areNotEmpty(vararg values: CharSequence): Boolean {
    for (value in values) if (value.isBlank()) return false
    return true
}

fun <K, V> get(key: K, getter: (K) -> V?, setter: (K, V) -> Unit, creator: () -> V): V =
        getter.invoke(key) ?: { creator.invoke().apply { setter.invoke(key, this) } }()

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


