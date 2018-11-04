package com.mainstreetcode.teammate.adapters.viewholders.input;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.util.Calendar;

import androidx.arch.core.util.Function;

import static java.util.Calendar.DATE;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static java.util.Calendar.getInstance;

/**
 * ViewHolder for selecting {@link com.mainstreetcode.teammate.model.Role}
 */
public class DateTextInputStyle extends TextInputStyle
        implements
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    private Calendar calendar = getInstance();

    public DateTextInputStyle(Function<Item, Boolean> enabler) {
        super(Item.EMPTY_CLICK, Item.EMPTY_CLICK, enabler, Item.ALL_INPUT_VALID, item -> R.drawable.ic_access_time_white_24dp);
    }

    @Override boolean isSelector() { return true; }

    @Override void onTextClicked(View view) { showDate(view.getContext()); }

    @Override public void onButtonClicked(View view) { showTime(view.getContext()); }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        calendar.set(YEAR, year);
        calendar.set(MONTH, month);
        calendar.set(DATE, day);

        updateTime();
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
        calendar.set(HOUR_OF_DAY, hourOfDay);
        calendar.set(MINUTE, minute);

        updateTime();
    }

    private void showDate(Context context) {
        if (!isEnabled()) return;
        AlertDialog dialog = new DatePickerDialog(context, this, calendar.get(YEAR), calendar.get(MONTH), calendar.get(DATE));
        dialog.setOnDismissListener(dialogInterface -> onDialogDismissed(context));
        dialog.show();
    }

    private void showTime(Context context) {
        if (!isEnabled()) return;
        AlertDialog dialog = new TimePickerDialog(context, this, calendar.get(HOUR_OF_DAY), calendar.get(MINUTE), true);
        dialog.setOnDismissListener(dialogInterface -> onDialogDismissed(context));
        dialog.show();
    }

    private void updateTime() {
        String updatedDate = ModelUtils.prettyPrinter.format(calendar.getTime());
        item.setValue(updatedDate);
        if (viewHolder != null) viewHolder.updateText(updatedDate);
    }
}
