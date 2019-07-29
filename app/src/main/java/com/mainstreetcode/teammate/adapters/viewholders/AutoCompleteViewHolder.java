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

package com.mainstreetcode.teammate.adapters.viewholders;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;

import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.AutoCompleteAdapter;
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder;

public class AutoCompleteViewHolder extends BaseViewHolder<AutoCompleteAdapter.AdapterListener> {

    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    private AutocompletePrediction prediction;

   private TextView title;
   private TextView subTitle;

    @SuppressLint("ClickableViewAccessibility")
    public AutoCompleteViewHolder(View itemView, AutoCompleteAdapter.AdapterListener adapterListener) {
        super(itemView, adapterListener);

        title = itemView.findViewById(R.id.title);
        subTitle = itemView.findViewById(R.id.sub_title);

        itemView.setOnClickListener(view -> adapterListener.onPredictionClicked(prediction));
    }

    public void bind(AutocompletePrediction prediction) {
        this.prediction = prediction;

        title.setText(prediction.getPrimaryText(STYLE_BOLD));
        subTitle.setText(prediction.getSecondaryText(STYLE_BOLD));
    }
}
