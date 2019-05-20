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

import android.view.View;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.StatTypeAdapter;
import com.mainstreetcode.teammate.model.Stat;
import com.mainstreetcode.teammate.model.enums.StatAttribute;
import com.mainstreetcode.teammate.model.enums.StatType;
import com.mainstreetcode.teammate.model.enums.StatTypes;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class StatAttributeViewHolder extends InputViewHolder {

    private final StatTypeAdapter adapter;
    private final StatType statType = StatType.empty();
    private final StatTypes statTypes;

    private RecyclerView recyclerView;

    public StatAttributeViewHolder(View itemView, Stat stat) {
        super(itemView);

        statTypes = stat.getSport().getStats();
        adapter = new StatTypeAdapter(new StatTypeAdapter.AdapterListener() {
            @Override
            public List<StatAttribute> getAttributes() {
                return isEnabled() ? statType.getAttributes() : stat.getAttributes();
            }

            @Override
            public boolean isEnabled() { return textInputStyle != null && textInputStyle.isEditable(); }

            @Override
            public boolean isSelected(StatAttribute attribute) { return stat.contains(attribute); }

            @Override
            public void onAttributeTapped(StatAttribute attribute) {
                if (!isEnabled()) return;
                stat.compoundAttribute(attribute);
                adapter.notifyDataSetChanged();
            }
        });

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(itemView.getContext());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        layoutManager.setAlignItems(AlignItems.CENTER);

        recyclerView = itemView.findViewById(R.id.inner_list);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override public void bind(TextInputStyle textInputStyle) {
        super.bind(textInputStyle);
        statType.update(statTypes.fromCodeOrFirst(textInputStyle.getItem().getRawValue()));
        adapter.notifyDataSetChanged();
    }

    @Override void updateText(CharSequence text) {
        super.updateText(text);
        if (textInputStyle == null) return;
        String value = textInputStyle.getItem().getRawValue();
        StatType current = null;

        for (StatType type : statTypes)
            if (type.getCode().equals(value)) {
                current = type;
                break;
            }
        if (current == null) return;
        statType.update(current);
        adapter.notifyDataSetChanged();
    }

    @Override protected float getHintLateralTranslation() {
        return super.getHintLateralTranslation();
    }

    @Override protected float getHintLongitudinalTranslation() {
        return -((itemView.getHeight() - hint.getHeight() - recyclerView.getHeight()) * 0.5F);
    }
}
