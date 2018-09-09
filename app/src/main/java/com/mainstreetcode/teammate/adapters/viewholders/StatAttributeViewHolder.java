package com.mainstreetcode.teammate.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.StatTypeAdapter;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.Stat;
import com.mainstreetcode.teammate.model.enums.StatAttribute;
import com.mainstreetcode.teammate.model.enums.StatType;
import com.mainstreetcode.teammate.util.Supplier;

import java.util.List;

public class StatAttributeViewHolder extends SelectionViewHolder<StatType> {

    private final StatTypeAdapter adapter;
    private final StatType statType = StatType.empty();

    public StatAttributeViewHolder(View itemView,
                                   int titleRes,
                                   Stat stat,
                                   Supplier<Boolean> enabler,
                                   Supplier<Boolean> errorChecker) {

        super(itemView, titleRes, Config.getStatTypes(stat.getSport()), StatType::getEmojiAndName, StatType::getCode, enabler, errorChecker);

        adapter = new StatTypeAdapter(new StatTypeAdapter.AdapterListener() {
            @Override
            public List<StatAttribute> getAttributes() {
                return enabler.get() ? statType.getAttributes() : stat.getAttributes();
            }

            @Override
            public boolean isSelected(StatAttribute attribute) {
                return stat.contains(attribute);
            }

            @Override
            public void onAttributeTapped(StatAttribute attribute) {
                if (!enabler.get()) return;
                stat.compoundAttribute(attribute);
                adapter.notifyDataSetChanged();
            }
        });

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(itemView.getContext());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        layoutManager.setAlignItems(2);

        RecyclerView recyclerView = itemView.findViewById(R.id.inner_list);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void bind(Item item) {
        super.bind(item);
        statType.update(Config.statTypeFromCode(item.getRawValue()));
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onItemSelected(List<CharSequence> sequences, int position, StatType type) {
        super.onItemSelected(sequences, position, type);
        statType.update(type);
        adapter.notifyDataSetChanged();
    }
}
