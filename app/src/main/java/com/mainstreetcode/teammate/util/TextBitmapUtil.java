package com.mainstreetcode.teammate.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;

public class TextBitmapUtil {

    public static Bitmap getBitmapMarker(CharSequence text) {
        TextView textView = (TextView) LayoutInflater.from(App.getInstance()).inflate(R.layout.marker_layout, null);

        textView.setText(text);
        textView.measure(unSpecifiedMeasureSpec(), unSpecifiedMeasureSpec());

        int measuredWidth = textView.getMeasuredWidth();
        int measuredHeight = textView.getMeasuredHeight();

        textView.layout(0, 0, measuredWidth, measuredHeight);

        Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        textView.draw(canvas);

        return bitmap;
    }

    private static int unSpecifiedMeasureSpec() {
        return View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
    }
}
