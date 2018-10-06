package com.mainstreetcode.teammate.adapters.viewholders;

import android.view.View;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.StatAggregate;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

public class StatAggregateViewHolder extends BaseViewHolder<BaseRecyclerViewAdapter.AdapterListener>{

    private TextView count;
    private TextView statType;

    public StatAggregateViewHolder(View itemView) {
        super(itemView, new BaseRecyclerViewAdapter.AdapterListener() {});
        count = itemView.findViewById(R.id.item_position);
        statType = itemView.findViewById(R.id.item_title);
    }

    public void bind(StatAggregate.Aggregate model) {
        count.setText(model.getCount());
        statType.setText(model.getType());
    }
}
