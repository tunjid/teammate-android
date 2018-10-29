package com.mainstreetcode.teammate.adapters.viewholders;

import androidx.arch.core.util.Function;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import android.view.View;

import com.mainstreetcode.teammate.util.Supplier;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;


public class SelectionViewHolder<T> extends ClickInputViewHolder
        implements View.OnClickListener {

    private final int titleRes;
    private final List<T> items;
    private final Function<T, CharSequence> displayFunction;
    private final Function<T, String> valueFunction;

    public SelectionViewHolder(View itemView,
                               @StringRes int titleRes,  List<T> items,
                               Function<T, CharSequence> displayFunction,
                               Function<T, String> valueFunction,
                               Supplier<Boolean> enabler) {
        super(itemView, enabler, () -> {});
        this.titleRes = titleRes;
        this.items = items;
        this.displayFunction = displayFunction;
        this.valueFunction = valueFunction;
    }

    public SelectionViewHolder(View itemView,
                               @StringRes int titleRes,  List<T> items,
                               Function<T, CharSequence> displayFunction,
                               Function<T, String> valueFunction,
                               Supplier<Boolean> enabler,
                               Function<CharSequence, CharSequence> errorChecker) {
        super(itemView, enabler, () -> {}, errorChecker);
        this.titleRes = titleRes;
        this.items = items;
        this.displayFunction = displayFunction;
        this.valueFunction = valueFunction;
    }

    @Override
    public void onClick(View view) {
        if (!isEnabled()) return;

        List<CharSequence> sequences = new ArrayList<>();

        Flowable.fromIterable(items)
                .map(displayFunction::apply)
                .collectInto(sequences, List::add)
                .subscribe();

        AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                .setTitle(titleRes)
                .setItems(sequences.toArray(new CharSequence[sequences.size()]), (dialogInterface, position) -> onItemSelected(sequences, position, items.get(position)))
                .create();

        dialog.setOnDismissListener(dialogInterface -> onDialogDismissed());
        dialog.show();
    }

    protected void onItemSelected(List<CharSequence> sequences, int position, T type) {
        item.setValue(valueFunction.apply(type));
        editText.setText(sequences.get(position).toString());
        editText.setError(null);
    }
}
