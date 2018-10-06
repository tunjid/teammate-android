package com.mainstreetcode.teammate.util.nav;

import android.view.MotionEvent;

import com.mainstreetcode.teammate.util.GestureListener;

class NavGestureListener extends GestureListener {

    private final ViewHolder viewHolder;
    private boolean hasSwiped;

    NavGestureListener(ViewHolder viewHolder) {this.viewHolder = viewHolder;}

    @Override
    public boolean onDown(MotionEvent e) { return !(hasSwiped = false); }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (hasSwiped) return false;

        float x1 = e1.getX();
        float y1 = e1.getY();

        float x2 = e2.getX();
        float y2 = e2.getY();

        Direction direction = getDirection(x1, y1, x2, y2);
        if (direction != Direction.up) return false;

        hasSwiped = true;
        viewHolder.onSwipedUp();
        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        viewHolder.click();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        super.onLongPress(e);
        viewHolder.onSwipedUp();
    }
}
