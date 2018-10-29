package com.mainstreetcode.teammate.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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

        TextView textView = new TextView(App.getInstance());

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
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }
}
