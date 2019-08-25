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

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment
import com.mainstreetcode.teammate.model.HeaderedModel
import com.mainstreetcode.teammate.util.DiffWatcher
import com.mainstreetcode.teammate.util.FULL_RES_LOAD_DELAY
import com.mainstreetcode.teammate.util.THUMBNAIL_SIZE
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder
import java.io.File

class HeaderedImageViewHolder(
        itemView: View,
        listener: ImageWorkerFragment.ImagePickerListener
) : InteractiveViewHolder<ImageWorkerFragment.ImagePickerListener>(itemView, listener), View.OnClickListener {

    private val fullRes: ImageView = itemView.findViewById(R.id.image_full_res)
    val thumbnail: ImageView = itemView.findViewById(R.id.image)

    private val diff: DiffWatcher<String>

    init {
        thumbnail.setOnClickListener(this)
        diff = DiffWatcher(this::getImage)
        animateHeader()
    }

    override fun onClick(view: View) {
        adapterListener.onImageClick()
    }

    fun bind(model: HeaderedModel<*>) {
        val url = model.headerItem.rawValue
        if (url.isBlank()) return

        diff.push(url.toString())
    }

    fun unBind() {
        diff.stop()
    }

    private fun getImage(url: String) {
        val creator = getCreator(url) ?: return

        creator.resize(THUMBNAIL_SIZE, THUMBNAIL_SIZE).centerInside().into(thumbnail)
        fullRes.postDelayed(DeferredImageLoader(url), FULL_RES_LOAD_DELAY.toLong())
    }

    private fun getCreator(url: String): RequestCreator? {
        if (url.isBlank()) return null

        val file = File(url)
        val picasso = Picasso.get()
        return if (file.exists()) picasso.load(file) else picasso.load(url)
    }

    private fun animateHeader() {
        val endColor = ContextCompat.getColor(itemView.context, R.color.black_50)
        val startColor = Color.TRANSPARENT

        val animator = ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor)
        animator.duration = 2000
        animator.addUpdateListener { animation ->
            val color = animation.animatedValue as? Int ?: return@addUpdateListener
            thumbnail.setColorFilter(color)
            fullRes.setColorFilter(color)
        }
        animator.start()
    }

    private inner class DeferredImageLoader internal constructor(private val url: String) : Runnable, Callback {

        override fun run() {
            val delayed = getCreator(url)
            delayed?.fit()?.centerCrop()?.into(fullRes, this)
        }

        override fun onSuccess() {
            fullRes.visibility = View.VISIBLE
        }

        override fun onError(e: Exception) = diff.restart()
    }
}
