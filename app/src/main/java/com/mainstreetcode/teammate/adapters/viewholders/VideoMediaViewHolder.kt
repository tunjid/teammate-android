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

package com.mainstreetcode.teammate.adapters.viewholders

import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.view.ViewCompat
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.devbrackets.android.exomedia.ui.widget.VideoView
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.MediaAdapterListener
import com.mainstreetcode.teammate.model.Media


class VideoMediaViewHolder @SuppressLint("ClickableViewAccessibility")
constructor(
        itemView: View,
        delegate: MediaAdapterListener
) : MediaViewHolder<VideoView>(itemView, delegate) {

    override val thumbnailId: Int
        get() = R.id.thumbnail

    override val fullViewId: Int
        get() = R.id.video_thumbnail

    init {
        fullResView.setOnTouchListener(TouchListener(itemView.context))

        if (delegate.isFullScreen) {
            val params = fullResView.findViewById<View>(R.id.exomedia_video_view).layoutParams as RelativeLayout.LayoutParams
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP)
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)

            val videoControls = fullResView.videoControls
            if (videoControls != null && videoControls.childCount < 1) {
                val inner = videoControls.getChildAt(0)
                ViewCompat.setOnApplyWindowInsetsListener(inner) { controls, insets ->
                    controls.setPadding(0, 0, 0, 0)
                    insets
                }
            }
        } else {
            fullResView.visibility = View.GONE
            itemView.setOnClickListener { delegate.onMediaClicked(media) }
        }
    }

    override fun fullBind(media: Media) {
        super.fullBind(media)

        itemView.setBackgroundResource(R.color.black)

        val videoUrl = media.url
        if (videoUrl.isBlank()) return

        fullResView.setVideoPath(videoUrl)
        fullResView.setOnPreparedListener {
            TransitionManager.beginDelayedTransition(itemView as ViewGroup, Fade())
            fullResView.visibility = View.VISIBLE
            fullResView.start()
        }
        fullResView.setOnCompletionListener {
            fullResView.setOnPreparedListener(null)
            fullResView.setVideoPath(videoUrl)
        }
    }

    override fun unBind() {
        super.unBind()
        fullResView.visibility = View.INVISIBLE
        fullResView.stopPlayback()
    }

    private inner class TouchListener internal constructor(context: Context) : GestureDetector.SimpleOnGestureListener(), View.OnTouchListener {
        internal var gestureDetector: GestureDetector = GestureDetector(context, this)

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            gestureDetector.onTouchEvent(event)
            return true
        }

        override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
            val videoControls = fullResView.videoControlsCore

            if (videoControls != null && videoControls.isVisible) videoControls.hide(false)
            else fullResView.showControls()

            delegate.onMediaClicked(media)
            return true
        }
    }
}
