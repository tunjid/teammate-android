package android.support.design.widget;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;

@SuppressWarnings("unused")
public class SwipeRefreshPassThroughBottomSheetBehavior extends BottomSheetBehavior {

    public SwipeRefreshPassThroughBottomSheetBehavior() { super(); }

    public SwipeRefreshPassThroughBottomSheetBehavior(Context context, AttributeSet attrs) { super(context, attrs); }

    @Override
    View findScrollingChild(View view) {
        View result = super.findScrollingChild(view);
        if (result == null || !(result instanceof SwipeRefreshLayout)) return result;

        result.post(() -> result.setEnabled(false));
        return ((SwipeRefreshLayout) result).getChildAt(0);
    }
}
