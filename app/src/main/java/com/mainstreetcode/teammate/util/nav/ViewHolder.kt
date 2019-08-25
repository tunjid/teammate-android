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

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.GestureDetectorCompat
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.util.resolveThemeColor
import de.hdodenhof.circleimageview.CircleImageView

class ViewHolder @SuppressLint("ClickableViewAccessibility")
internal constructor(internal val itemView: View, private val swipeRunnable: () -> Unit) {

    private var hasCustomImage: Boolean = false

    private lateinit var navItem: NavItem
    private val title: TextView = itemView.findViewById(R.id.item_title)
    private val icon: CircleImageView = itemView.findViewById(R.id.thumbnail)
//    private val callback: ImageCallback = ImageCallback(this)

    init {
        icon.isDisableCircularTransformation = true

        val detector = GestureDetectorCompat(itemView.context, NavGestureListener(this))
        itemView.setOnTouchListener { v, event -> onItemViewTouched(detector, v, event) }
    }

//    fun setImageUrl(imageUrl: String) = callback.loadUrl(imageUrl)

    internal fun click() {
        itemView.performClick()
    }

    internal fun onSwipedUp() = swipeRunnable.invoke()

    private fun onItemViewTouched(detector: GestureDetectorCompat, v: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> v.isPressed = true
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_OUTSIDE -> v.isPressed = false
        }
        return detector.onTouchEvent(event)
    }

    internal fun tint(@AttrRes colorAttr: Int) {
        val color = itemView.context.resolveThemeColor(colorAttr)
        title.setTextColor(color)
        icon.borderColor = color
        if (hasCustomImage) return

        var drawable: Drawable? = icon.drawable ?: return

        drawable = drawable!!.mutate()
        drawable = DrawableCompat.wrap(drawable)
        DrawableCompat.setTint(drawable!!, color)
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN)
        icon.setImageDrawable(drawable)
    }

    internal fun bind(navItem: NavItem): ViewHolder {
        this.navItem = navItem
        itemView.id = navItem.idRes
        title.setText(navItem.titleRes)
        icon.setImageResource(navItem.drawableRes)

        tint(R.attr.bottom_nav_unselected)

        return this
    }

    internal fun setOnClickListener(listener: View.OnClickListener): ViewHolder {
        itemView.setOnClickListener(listener)
        return this
    }

//    private fun onCustomImageLoaded(succeeded: Boolean) {
//        hasCustomImage = succeeded
//        icon.isDisableCircularTransformation = !succeeded
//        icon.borderWidth = if (succeeded) itemView.resources.getDimensionPixelSize(R.dimen.sixteenth_margin) else 0
//        if (!hasCustomImage) bind(navItem)
//    }

//    private class ImageCallback internal constructor(internal val viewHolder: ViewHolder) : Callback {
//        internal var loaded = false
//        internal var currentImage: String? = null
//
//        internal fun loadUrl(imageUrl: String) {
//            val sameImage = currentImage != null && currentImage == imageUrl
//            if (sameImage && loaded || TextUtils.isEmpty(imageUrl)) return
//
//            currentImage = imageUrl
//            viewHolder.hasCustomImage = true
//            Picasso.get().load(imageUrl).fit().centerCrop().noFade().into(viewHolder.icon, this)
//        }
//
//        override fun onSuccess() {
//            viewHolder.onCustomImageLoaded(true.apply { loaded = this })
//        }
//
//        override fun onError(e: Exception) {
//            viewHolder.onCustomImageLoaded(false.apply { loaded = this })
//        }
//    }
}
