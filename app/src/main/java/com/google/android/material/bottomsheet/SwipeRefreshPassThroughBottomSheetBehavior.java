package com.google.android.material.bottomsheet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

@SuppressWarnings("unused")
public class SwipeRefreshPassThroughBottomSheetBehavior extends BottomSheetBehavior {

    public SwipeRefreshPassThroughBottomSheetBehavior() { super(); }

    public SwipeRefreshPassThroughBottomSheetBehavior(Context context, AttributeSet attrs) { super(context, attrs); }

    @Override
    View findScrollingChild(View view) {
        @SuppressLint("VisibleForTests") View result = super.findScrollingChild(view);
        if (!(result instanceof SwipeRefreshLayout)) return result;

        result.post(() -> result.setEnabled(false));
        return ((SwipeRefreshLayout) result).getChildAt(0);
    }
}
