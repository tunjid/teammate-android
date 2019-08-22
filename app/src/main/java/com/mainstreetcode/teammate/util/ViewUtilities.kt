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

package com.mainstreetcode.teammate.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import com.mainstreetcode.teammate.R
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

const val ITEM = 283
const val USER = 284
const val TEAM = 285
const val CHAT = 286
const val ROLE = 287
const val GAME = 288
const val HOME = 289
const val AWAY = 290
const val STAT = 291
const val EVENT = 292
const val GUEST = 292
const val FEED_ITEM = 294
const val TOURNAMENT = 295
const val CONTENT_AD = 296
const val INSTALL_AD = 297
const val MEDIA_IMAGE = 298
const val MEDIA_VIDEO = 299
const val JOIN_REQUEST = 300
const val BLOCKED_USER = 301
const val THUMBNAIL_SIZE = 250
const val FULL_RES_LOAD_DELAY = 200
const val TOOLBAR_ANIM_DELAY = 200

fun View.isDisplayingSystemUI(): Boolean =
        systemUiVisibility and View.SYSTEM_UI_FLAG_FULLSCREEN != 0

fun Any.getTransitionName(@IdRes id: Int): String =
        hashCode().toString() + "-" + id

@ColorInt
fun Context.resolveThemeColor(@AttrRes colorAttr: Int): Int {
    val typedValue = TypedValue()
    val theme = theme
    theme.resolveAttribute(colorAttr, typedValue, true)
    return typedValue.data
}

fun Context.getActivity(): Activity? {
    var unwrapped = this
    while (unwrapped is ContextWrapper)
        if (unwrapped is Activity) return unwrapped
        else unwrapped = unwrapped.baseContext

    return null
}

fun Toolbar.updateToolBar(menu: Int, title: CharSequence) {
    val childCount = childCount

    if (id == R.id.alt_toolbar || childCount <= 2) {
        this.title = title
        this.replaceMenu(menu)
    } else for (i in 0 until childCount) {
        val child = getChildAt(i)
        if (child is ImageView) continue

        child.animate().alpha(0f).setDuration(TOOLBAR_ANIM_DELAY.toLong()).withEndAction {
            if (child is TextView) this.title = title
            else if (child is ActionMenuView) this.replaceMenu(menu)

            child.animate()
                    .setDuration(TOOLBAR_ANIM_DELAY.toLong())
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .alpha(1f).start()
        }.start()
    }
}

private fun Toolbar.replaceMenu(menu: Int) {
    this.menu.clear()
    if (menu != 0) inflateMenu(menu)
}

@JvmOverloads
fun fetchRoundedDrawable(context: Context, url: String, size: Int, placeholder: Int = 0): Maybe<Drawable> = Maybe.create<Bitmap> { emitter ->
    Picasso.get().load(url).resize(size, size).centerCrop()
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                    if (!emitter.isDisposed) emitter.onSuccess(bitmap)
                }

                override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
                    if (!emitter.isDisposed) emitter.onError(e)
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) = Unit
            })
}
        .onErrorResumeNext { throwable: Throwable ->
            if (placeholder != 0) Maybe.fromCallable {
                ContextCompat.getDrawable(context, placeholder)
                        ?.run { DrawableCompat.wrap(this).apply { setTint(Color.WHITE) } }
                        ?.run { toBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888) }
            }
            else Maybe.error(throwable)
        }
        .map { bitmap ->
            val imageDrawable = RoundedBitmapDrawableFactory.create(context.resources, bitmap)
            imageDrawable.isCircular = true
            imageDrawable.cornerRadius = size.toFloat()
            imageDrawable
        }

fun extractPalette(imageView: ImageView): Single<Palette> {
    val drawable = (imageView.drawable
            ?: return Single.error(TeammateException("No drawable in ImageView"))) as? BitmapDrawable
            ?: return Single.error(TeammateException("Not a BitmapDrawable"))

    return Single.fromCallable { Palette.from(drawable.bitmap).generate() }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}
