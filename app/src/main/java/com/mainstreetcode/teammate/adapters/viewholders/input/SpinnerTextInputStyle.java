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
        super(Item.EMPTY_CLICK, Item.EMPTY_CLICK, enabler, errorChecker, Item.NO_ICON);
        this.titleRes = titleRes;
        this.items = items;
        this.displayFunction = displayFunction;
        this.valueFunction = valueFunction;
    }

    @Override void onTextClicked(View view) {
        if (!isEditable()) return;

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
        if (viewHolder == null) return;
        viewHolder.updateText(sequences.get(position));
    }
}
