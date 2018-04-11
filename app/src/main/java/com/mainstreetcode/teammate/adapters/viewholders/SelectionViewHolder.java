package com.mainstreetcode.teammate.adapters.viewholders;

import android.arch.core.util.Function;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.mainstreetcode.teammate.util.Supplier;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;


public class SelectionViewHolder<T> extends ClickInputViewHolder
        implements View.OnClickListener {

    private final int titleRes;
    private final List<T> items;
    private final Function<T, CharSequence> textTransformer;
    private final Function<T, String> stringTransformer;

    public SelectionViewHolder(View itemView,
                               @StringRes int titleRes,  List<T> items,
                               Function<T, CharSequence> textTransformer,
                               Function<T, String> stringTransformer,
                               Supplier<Boolean> enabler) {
        super(itemView, enabler, () -> {});
        this.titleRes = titleRes;
        this.items = items;
        this.textTransformer = textTransformer;
        this.stringTransformer = stringTransformer;
        editText.removeTextChangedListener(this);
    }

    public SelectionViewHolder(View itemView,
                               @StringRes int titleRes,  List<T> items,
                               Function<T, CharSequence> textTransformer,
                               Function<T, String> stringTransformer,
                               Supplier<Boolean> enabler,
                               Supplier<Boolean> errorChecker) {
        super(itemView, enabler, () -> {}, errorChecker);
        this.titleRes = titleRes;
        this.items = items;
        this.textTransformer = textTransformer;
        this.stringTransformer = stringTransformer;
        editText.removeTextChangedListener(this);
    }

    @Override
    public void onClick(View view) {
        if (!isEnabled()) return;

        List<CharSequence> sequences = new ArrayList<>();

        Flowable.fromIterable(items)
                .map(textTransformer::apply)
                .collectInto(sequences, List::add)
                .subscribe();

        AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                .setTitle(titleRes)
                .setItems(sequences.toArray(new CharSequence[sequences.size()]), (dialogInterface, position) -> {
                    T type = items.get(position);
                    item.setValue(stringTransformer.apply(type));
                    editText.setText(sequences.get(position).toString());
                    editText.setError(null);
                })
                .create();

        dialog.setOnDismissListener(dialogInterface -> onDialogDismissed());
        dialog.show();
    }
}
