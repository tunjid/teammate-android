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
import com.mainstreetcode.teammate.util.ErrorHandler;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Flowable;

public class StatAttributeViewHolder extends InputViewHolder {

    private final StatTypeAdapter adapter;
    private final StatType statType = StatType.empty();
    private final StatTypes statTypes;

    public StatAttributeViewHolder(View itemView, Stat stat) {
        super(itemView);

        statTypes = stat.getSport().getStats();
        adapter = new StatTypeAdapter(new StatTypeAdapter.AdapterListener() {
            @Override
            public List<StatAttribute> getAttributes() {
                return isEnabled() ? statType.getAttributes() : stat.getAttributes();
            }

            @Override
            public boolean isEnabled() { return textInputStyle != null && textInputStyle.isEnabled(); }

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

        RecyclerView recyclerView = itemView.findViewById(R.id.inner_list);
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
}
