package com.mainstreetcode.teammate.adapters.viewholders;

import android.view.View;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Stat;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;


public class StatViewHolder extends BaseViewHolder<BaseRecyclerViewAdapter.AdapterListener> {

    protected Stat model;

    private TextView homeStatTime;
    private TextView homeStatUser;
    private TextView homeStatName;
    private TextView awayStatTime;
    private TextView awayStatUser;
    private TextView awayStatName;

    public StatViewHolder(View itemView, BaseRecyclerViewAdapter.AdapterListener adapterListener) {
        super(itemView, adapterListener);
        homeStatTime = itemView.findViewById(R.id.home_stat_time);
        homeStatUser = itemView.findViewById(R.id.home_stat_user);
        homeStatName = itemView.findViewById(R.id.home_stat_name);
        awayStatTime = itemView.findViewById(R.id.away_stat_time);
        awayStatUser = itemView.findViewById(R.id.away_stat_user);
        awayStatName = itemView.findViewById(R.id.away_stat_name);
    }

    public void bind(Stat model) {
        this.model = model;
        boolean isHome = model.isHome();
        TextView time = isHome ? homeStatTime : awayStatTime;
        TextView user = isHome ? homeStatUser : awayStatUser;
        TextView name = isHome ? homeStatName : awayStatName;

        time.setText(String.valueOf(model.getTime()));
        user.setText(model.getUser().getFirstName());
        name.setText(model.getName());
    }
}
