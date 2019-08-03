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

package com.mainstreetcode.teammate.adapters.viewholders.input

import android.content.Context
import android.util.SparseArray
import android.view.View
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseActivity
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.util.ModelUtils
import com.mainstreetcode.teammate.util.ViewHolderUtil.getActivity

open class TextInputStyle(
        private val textRunnable: (() -> Unit)?,
        private val buttonRunnable: (() -> Unit)?,
        private val enabler: (Item<*>) -> Boolean,
        private val errorChecker: (Item<*>) -> CharSequence?,
        private val iconVisibilityFunction: (Item<*>) -> Int) {

    lateinit var item: Item<*>
    var viewHolder: InputViewHolder<*>? = null

    internal open val isSelector: Boolean
        get() = textRunnable != null

    internal val isEditable: Boolean
        get() = enabler.invoke(item)

    internal val icon: Int
        get() = iconVisibilityFunction.invoke(item)

    constructor(
            enabler: (Item<*>) -> Boolean,
            errorChecker: (Item<*>) -> CharSequence,
            iconVisibilityFunction: (Item<*>) -> Int
    ) : this(Item.NO_CLICK, Item.NO_CLICK, enabler, errorChecker, iconVisibilityFunction)

    internal fun errorText(): CharSequence? = errorChecker.invoke(item)

    internal fun buttonClickListener(): View.OnClickListener? =
            if (icon != 0) View.OnClickListener { this.onButtonClicked(it) } else null

    internal fun textClickListener(): View.OnClickListener? =
            if (isEditable) View.OnClickListener { this.onTextClicked(it) } else null

    internal open fun onTextClicked(view: View) = textRunnable?.invoke()

    internal open fun onButtonClicked(view: View) = buttonRunnable?.invoke()

    internal fun onDialogDismissed(context: Context) {
        val activity = getActivity(context)
        if (activity is TeammatesBaseActivity) activity.onDialogDismissed()
    }

    private fun with(item: Item<*>): TextInputStyle {
        this.item = item
        return this
    }

    abstract class InputChooser : (Item<*>) -> TextInputStyle {

        private val behaviors = SparseArray<TextInputStyle>()

        operator fun get(item: Item<*>): TextInputStyle = ModelUtils.get(
                item.stringRes,
                { behaviors.get(it) },
                { key, value -> behaviors.put(key, value) },
                { invoke(item) }
        ).with(item)

        open fun iconGetter(item: Item<*>): Int = 0

        open fun enabler(item: Item<*>): Boolean = false

        open fun textChecker(item: Item<*>): CharSequence? = null
    }
}

fun <T> or(check: Boolean, a: T, b: T) = if (check) a else b