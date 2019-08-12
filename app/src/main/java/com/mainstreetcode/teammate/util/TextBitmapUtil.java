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

package com.mainstreetcode.teammate.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;
import android.view.View;
import android.widget.TextView;

import com.mainstreetcode.teammate.App;

public class TextBitmapUtil {

    private static LruCache<String, Bitmap> cache = buildCache();

    public static Bitmap getBitmapMarker(CharSequence text) {
        String key = text.toString();
        Bitmap bitmap = cache.get(key);
        if (bitmap != null) return bitmap;

        TextView textView = new TextView(App.Companion.getInstance());

        textView.setTextColor(Color.BLACK);
        textView.setText(text);
        textView.measure(unSpecifiedMeasureSpec(), unSpecifiedMeasureSpec());

        int measuredWidth = textView.getMeasuredWidth();
        int measuredHeight = textView.getMeasuredHeight();

        textView.layout(0, 0, measuredWidth, measuredHeight);

        bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        textView.draw(canvas);
        cache.put(key, bitmap);

        return bitmap;
    }

    private static int unSpecifiedMeasureSpec() {
        return View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
    }

    private static LruCache<String, Bitmap> buildCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = Math.min(maxMemory / 2056, 128);

        return new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(@NonNull String key, @NonNull Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }
}
