package com.mainstreetcode.teammate.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.widget.TextView;

import com.mainstreetcode.teammate.App;

import java.util.HashMap;
import java.util.Map;

public class TextBitmapUtil {

    private static Map<String, Bitmap> cache = new HashMap<>();

    public static Bitmap getBitmapMarker(CharSequence text) {
        String key = text.toString();
        Bitmap bitmap = cache.get(key);
        if (bitmap != null) return bitmap;

        TextView textView = new TextView(App.getInstance());

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
}
