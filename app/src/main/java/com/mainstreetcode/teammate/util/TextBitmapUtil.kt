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

import android.graphics.Bitmap
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.collection.LruCache
import androidx.core.view.drawToBitmap
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.R
import kotlin.math.min

object TextBitmapUtil {

    private val cache = buildCache()

    fun getBitmapMarker(text: CharSequence): Bitmap {
        val key = text.toString()
        cache.get(key)?.let { return it }

        val app = App.instance
        val size = app.resources.getDimensionPixelSize(R.dimen.double_margin)
        val textView = TextView(app)

        textView.setTextColor(Color.BLACK)
        textView.text = text
        textView.measure(unSpecifiedMeasureSpec(size), unSpecifiedMeasureSpec(size))

        val measuredWidth = textView.measuredWidth
        val measuredHeight = textView.measuredHeight

        textView.layout(0, 0, measuredWidth, measuredHeight)

        return textView.drawToBitmap(Bitmap.Config.ARGB_8888).apply { cache.put(key, this) }
    }

    private fun unSpecifiedMeasureSpec(size: Int): Int =
            View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)

    private fun buildCache(): LruCache<String, Bitmap> {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = min(maxMemory / 2056, 128)

        return object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int = bitmap.byteCount / 1024
        }
    }
}
