package com.mainstreetcode.teammate.adapters.viewholders;

import android.view.View;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.StatAggregate;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

public class StatAggregateViewHolder extends InteractiveViewHolder<InteractiveAdapter.AdapterListener>{

    private TextView count;
    private TextView statType;

    public StatAggregateViewHolder(View itemView) {
        super(itemView, new InteractiveAdapter.AdapterListener() {});
        count = itemView.findViewById(R.id.item_position);
        statType = itemView.findViewById(R.id.item_title);
    }

    public void bind(StatAggregate.Aggregate model) {
        count.setText(model.getCount());
        statType.setText(model.getType());
    }
}
