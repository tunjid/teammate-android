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

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.util.prettyPrint
import java.util.Calendar.DATE
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE
import java.util.Calendar.MONTH
import java.util.Calendar.YEAR
import java.util.Calendar.getInstance

/**
 * ViewHolder for selecting [com.mainstreetcode.teammate.model.Role]
 */
class DateTextInputStyle(enabler: (Item<*>) -> Boolean) : TextInputStyle(
        Item.EMPTY_CLICK,
        Item.EMPTY_CLICK,
        enabler,
        Item.ALL_INPUT_VALID,
        { R.drawable.ic_access_time_white_24dp }
), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private val calendar = getInstance()

    override val isSelector: Boolean
        get() = true

    override fun onTextClicked(view: View) = showDate(view.context)

    public override fun onButtonClicked(view: View) = showTime(view.context)

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        calendar.set(YEAR, year)
        calendar.set(MONTH, month)
        calendar.set(DATE, day)

        updateTime()
    }

    override fun onTimeSet(timePicker: TimePicker, hourOfDay: Int, minute: Int) {
        calendar.set(HOUR_OF_DAY, hourOfDay)
        calendar.set(MINUTE, minute)

        updateTime()
    }

    private fun showDate(context: Context) {
        if (!isEditable) return
        val dialog = DatePickerDialog(context, this, calendar.get(YEAR), calendar.get(MONTH), calendar.get(DATE))
        dialog.setOnDismissListener { onDialogDismissed(context) }
        dialog.show()
    }

    private fun showTime(context: Context) {
        if (!isEditable) return
        val dialog = TimePickerDialog(context, this, calendar.get(HOUR_OF_DAY), calendar.get(MINUTE), true)
        dialog.setOnDismissListener { onDialogDismissed(context) }
        dialog.show()
    }

    private fun updateTime() {
        val updatedDate =  calendar.time.prettyPrint()
        item.setValue(updatedDate)
        viewHolder?.updateText(updatedDate)
    }
}
