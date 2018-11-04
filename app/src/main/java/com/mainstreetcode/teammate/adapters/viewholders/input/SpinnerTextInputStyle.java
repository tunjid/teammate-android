package com.mainstreetcode.teammate.adapters.viewholders.input;

import android.content.Context;
import android.view.View;

import com.mainstreetcode.teammate.model.Item;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.arch.core.util.Function;
import io.reactivex.Flowable;


public class SpinnerTextInputStyle<T> extends TextInputStyle {

    private final int titleRes;
    private final List<T> items;
    private final Function<T, CharSequence> displayFunction;
    private final Function<T, String> valueFunction;

    public SpinnerTextInputStyle(@StringRes int titleRes, List<T> items,
                                 Function<T, CharSequence> displayFunction,
                                 Function<T, String> valueFunction,
                                 Function<Item, Boolean> enabler) {
        super(Item.EMPTY_CLICK, Item.EMPTY_CLICK, enabler, Item.NON_EMPTY, Item.NO_ICON);
        this.titleRes = titleRes;
        this.items = items;
        this.displayFunction = displayFunction;
        this.valueFunction = valueFunction;
    }

    public SpinnerTextInputStyle(@StringRes int titleRes, List<T> items,
                                 Function<T, CharSequence> displayFunction,
                                 Function<T, String> valueFunction,
                                 Function<Item, Boolean> enabler,
                                 Function<Item, CharSequence> errorChecker) {
        super(() -> {}, () -> {}, enabler, errorChecker, Item.NO_ICON);
        this.titleRes = titleRes;
        this.items = items;
        this.displayFunction = displayFunction;
        this.valueFunction = valueFunction;
    }

    @Override void onTextClicked(View view) {
        if (!isEnabled()) return;

        List<CharSequence> sequences = new ArrayList<>();

        Flowable.fromIterable(items)
                .map(displayFunction::apply)
                .collectInto(sequences, List::add)
                .subscribe();

        Context context = view.getContext();
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(titleRes)
                .setItems(sequences.toArray(new CharSequence[0]), (dialogInterface, position) -> onItemSelected(sequences, position, items.get(position)))
                .create();

        dialog.setOnDismissListener(dialogInterface -> onDialogDismissed(context));
        dialog.show();
    }

    private void onItemSelected(List<CharSequence> sequences, int position, T type) {
        item.setValue(valueFunction.apply(type));
        if (editText == null) return;
        editText.setText(sequences.get(position).toString());
        editText.setError(null);
    }
}
