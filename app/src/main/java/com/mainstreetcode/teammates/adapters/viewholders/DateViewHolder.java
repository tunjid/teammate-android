package com.mainstreetcode.teammates.adapters.viewholders;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Item;
import com.mainstreetcode.teammates.util.ModelUtils;
import com.mainstreetcode.teammates.util.Supplier;

import java.util.Calendar;

import static java.util.Calendar.DATE;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static java.util.Calendar.getInstance;

/**
 * ViewHolder for selecting {@link com.mainstreetcode.teammates.model.Role}
 */
public class DateViewHolder extends ClickInputViewHolder
        implements
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    private Calendar calendar = getInstance();
    private Calendar updatedCalendar = getInstance();

    public DateViewHolder(View itemView, Supplier<Boolean> enabler) {
        super(itemView, enabler, () -> {});
        itemView.findViewById(R.id.click_view).setOnClickListener(this);
    }

    @Override
    public void bind(Item item) {
        super.bind(item);
        String time = item.getValue();
        editText.setText(time);
        calendar.setTime(ModelUtils.parseDate(time, Event.prettyPrinter));
    }

    @Override
    public void onClick(View view) {
        if (!isEnabled()) return;

        AlertDialog dialog = new DatePickerDialog(itemView.getContext(), this, calendar.get(YEAR), calendar.get(MONTH), calendar.get(DATE));

        dialog.setOnDismissListener(dialogInterface -> onDialogDismissed());
        dialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        updatedCalendar.set(YEAR, year);
        updatedCalendar.set(MONTH, month);
        updatedCalendar.set(DATE, day);

        AlertDialog dialog = new TimePickerDialog(itemView.getContext(), this, calendar.get(HOUR_OF_DAY), calendar.get(MINUTE), true);

        dialog.setOnDismissListener(dialogInterface -> onDialogDismissed());
        dialog.show();
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
        updatedCalendar.set(HOUR_OF_DAY, hourOfDay);
        updatedCalendar.set(MINUTE, minute);

        String updatedDate = Event.prettyPrinter.format(updatedCalendar.getTime());

        item.setValue(updatedDate);
        editText.setText(updatedDate);

        calendar = updatedCalendar;
        updatedCalendar = Calendar.getInstance();
    }
}
