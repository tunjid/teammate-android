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

import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.model.noBlankFields
import com.mainstreetcode.teammate.model.noIcon


class SpinnerTextInputStyle<T> : TextInputStyle {

    private val titleRes: Int
    private val items: List<T>
    private val displayFunction: (T) -> CharSequence
    private val valueFunction: (T) -> String

    constructor(
            @StringRes titleRes: Int, items: List<T>,
            displayFunction: (T) -> CharSequence,
            valueFunction: (T) -> String,
            enabler: (Item) -> Boolean
    ) : super(Item.ignoreClicks, Item.ignoreClicks, enabler, Item::noBlankFields, Item::noIcon) {
        this.titleRes = titleRes
        this.items = items
        this.displayFunction = displayFunction
        this.valueFunction = valueFunction
    }

    constructor(@StringRes titleRes: Int, items: List<T>,
                displayFunction: (T) -> CharSequence,
                valueFunction: (T) -> String,
                enabler: (Item) -> Boolean,
                errorChecker: (Item) -> CharSequence?
    ) : super(Item.ignoreClicks, Item.ignoreClicks, enabler, errorChecker, Item::noIcon) {
        this.titleRes = titleRes
        this.items = items
        this.displayFunction = displayFunction
        this.valueFunction = valueFunction
    }

    override fun onTextClicked(view: View) {
        if (!isEditable) return

        val sequences = items.map { displayFunction.invoke(it) }


        val context = view.context
        val dialog = AlertDialog.Builder(context)
                .setTitle(titleRes)
                .setItems(sequences.toTypedArray()) { _, position -> onItemSelected(sequences, position, items[position]) }
                .create()

        dialog.setOnDismissListener { onDialogDismissed(context) }
        dialog.show()
    }

    private fun onItemSelected(sequences: List<CharSequence>, position: Int, type: T) {
        item.rawValue = valueFunction.invoke(type)
        viewHolder?.updateText(sequences[position])
    }
}
