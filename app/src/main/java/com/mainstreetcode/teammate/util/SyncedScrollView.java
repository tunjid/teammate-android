package com.mainstreetcode.teammate.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

public class SyncedScrollView extends HorizontalScrollView {

    private SyncedScrollManager scrollManager = null;

    public SyncedScrollView(Context context) {
        super(context);
    }

    public SyncedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SyncedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        super.onScrollChanged(scrollX, scrollY, oldScrollX, oldScrollY);
        if (scrollManager == null) return;
        scrollManager.onScrollChanged(this, scrollX, scrollY, oldScrollX, oldScrollY);
    }

    public void setScrollManager(SyncedScrollManager scrollManager) {
        this.scrollManager = scrollManager;
    }
}
