package com.mainstreetcode.teammate.adapters.viewholders;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.mainstreetcode.teammate.util.Supplier;

import java.util.Calendar;

import static java.util.Calendar.DATE;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static java.util.Calendar.getInstance;

/**
 * ViewHolder for selecting {@link com.mainstreetcode.teammate.model.Role}
 */
public class DateViewHolder extends ClickInputViewHolder
        implements
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    private Calendar calendar = getInstance();

    public DateViewHolder(View itemView, Supplier<Boolean> enabler) {
        super(itemView, enabler, () -> {});
        setButtonRunnable(R.drawable.ic_access_time_white_24dp, this::showTime);
    }

    @Override
    public void bind(Item item) {
        super.bind(item);
        String time = item.getValue().toString();
        editText.setText(time);
        calendar.setTime(ModelUtils.parseDate(time, ModelUtils.prettyPrinter));
    }

    @Override
    public void onClick(View view) { if(isEnabled())  showDate(); }

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

    private void showDate() {
        if (!isEnabled()) return;
        AlertDialog dialog = new DatePickerDialog(itemView.getContext(), this, calendar.get(YEAR), calendar.get(MONTH), calendar.get(DATE));
        dialog.setOnDismissListener(dialogInterface -> onDialogDismissed());
        dialog.show();
    }

    private void showTime() {
        if (!isEnabled()) return;
        AlertDialog dialog = new TimePickerDialog(itemView.getContext(), this, calendar.get(HOUR_OF_DAY), calendar.get(MINUTE), true);
        dialog.setOnDismissListener(dialogInterface -> onDialogDismissed());
        dialog.show();
    }

    private void updateTime() {
        String updatedDate = ModelUtils.prettyPrinter.format(calendar.getTime());
        item.setValue(updatedDate);
        editText.setText(updatedDate);
    }
}
