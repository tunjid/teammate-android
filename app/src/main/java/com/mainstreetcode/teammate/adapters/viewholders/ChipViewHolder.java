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

import androidx.annotation.NonNull;
import com.google.android.material.chip.Chip;
import android.view.View;

import com.mainstreetcode.teammate.adapters.StatTypeAdapter;
import com.mainstreetcode.teammate.model.enums.StatAttribute;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

public class ChipViewHolder extends InteractiveViewHolder<StatTypeAdapter.AdapterListener> {
    private Chip chip;
    private StatAttribute attribute;

    public ChipViewHolder(@NonNull View itemView, StatTypeAdapter.AdapterListener listener) {
        super(itemView, listener);
        chip = (Chip) itemView;
        chip.setOnClickListener(view -> adapterListener.onAttributeTapped(attribute));
    }

    public void bind(StatAttribute attribute) {
        this.attribute = attribute;
        chip.setText(attribute.getName());
        chip.setEnabled(adapterListener.isEnabled());
        chip.setChecked(adapterListener.isSelected(attribute));
    }
}
