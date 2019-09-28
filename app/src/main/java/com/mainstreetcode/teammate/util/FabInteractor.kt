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

import android.graphics.drawable.Drawable
import android.view.View

import com.google.android.material.button.MaterialButton
import com.tunjid.androidx.material.animator.FabExtensionAnimator

import java.util.Objects
import java.util.concurrent.atomic.AtomicBoolean

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

class FabInteractor(private val button: MaterialButton) : FabExtensionAnimator(button) {

    fun update(@DrawableRes icon: Int, @StringRes text: Int) =
            updateGlyphs(State(button, icon, text))

    fun setOnClickListener(clickListener: View.OnClickListener?) {
        if (clickListener == null) return  button.setOnClickListener(null)

        val flag = AtomicBoolean(true)

        button.setOnClickListener inner@{ view ->
            if (!flag.getAndSet(false)) return@inner

            clickListener.onClick(view)
            button.postDelayed({ flag.set(true) }, 2000)
        }
    }

    private inner class State internal constructor(
            button: MaterialButton,
            @param:DrawableRes @field:DrawableRes
            private val icon: Int,
            @param:StringRes @field:StringRes
            private val text: Int
    ) : FabExtensionAnimator.GlyphState() {

        private val charSequence: CharSequence = button.resources.getText(text)
        private val drawable: Drawable? = ContextCompat.getDrawable(button.context, icon)

        override fun getIcon(): Drawable? = drawable

        override fun getText(): CharSequence = charSequence

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val state = other as State?
            return icon == state!!.icon && text == state.text
        }

        override fun hashCode(): Int = Objects.hash(icon, text)
    }
}
