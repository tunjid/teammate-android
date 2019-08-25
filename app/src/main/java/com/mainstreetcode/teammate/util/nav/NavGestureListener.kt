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

package com.mainstreetcode.teammate.util.nav

import android.view.MotionEvent

import com.mainstreetcode.teammate.util.GestureListener

internal class NavGestureListener(private val viewHolder: ViewHolder) : GestureListener() {
    private var hasSwiped: Boolean = false

    override fun onDown(e: MotionEvent): Boolean {
        hasSwiped = false
        return !hasSwiped
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        if (hasSwiped) return false

        val x1 = e1.x
        val y1 = e1.y

        val x2 = e2.x
        val y2 = e2.y

        val direction = getDirection(x1, y1, x2, y2)
        if (direction != Direction.up) return false

        hasSwiped = true
        viewHolder.onSwipedUp()
        return super.onScroll(e1, e2, distanceX, distanceY)
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        viewHolder.click()
        return true
    }

    override fun onLongPress(e: MotionEvent) {
        super.onLongPress(e)
        viewHolder.onSwipedUp()
    }
}
