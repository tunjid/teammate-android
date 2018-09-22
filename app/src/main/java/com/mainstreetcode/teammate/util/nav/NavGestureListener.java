package com.mainstreetcode.teammate.util.nav;

import android.view.MotionEvent;

import com.mainstreetcode.teammate.util.GestureListener;

class NavGestureListener extends GestureListener {

    private final ViewHolder viewHolder;

    NavGestureListener(ViewHolder viewHolder) {this.viewHolder = viewHolder;}

    @Override
    public boolean onDown(MotionEvent e) { return true; }


    @Override
    public boolean onSwipe(Direction direction) {
        if (direction != Direction.up) return false;
        viewHolder.onSwipedUp();
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        viewHolder.click();
        return true;
    }
}
