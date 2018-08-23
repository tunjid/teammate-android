package com.mainstreetcode.teammate.adapters.viewholders;

import android.view.View;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Stat;
import com.mainstreetcode.teammate.model.enums.StatType;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;


public class StatViewHolder extends BaseViewHolder<ViewHolderUtil.SimpleAdapterListener<Stat>> {

    protected Stat model;

    private TextView homeStatTime;
    private TextView homeStatUser;
    private TextView homeStatName;
    private TextView homeStatEmoji;
    private TextView awayStatTime;
    private TextView awayStatUser;
    private TextView awayStatName;
    private TextView awayStatEmoji;

    public StatViewHolder(View itemView, ViewHolderUtil.SimpleAdapterListener<Stat> adapterListener) {
        super(itemView, adapterListener);
        homeStatTime = itemView.findViewById(R.id.home_stat_time);
        homeStatUser = itemView.findViewById(R.id.home_stat_user);
        homeStatName = itemView.findViewById(R.id.home_stat_name);
        homeStatEmoji = itemView.findViewById(R.id.home_stat_emoji);
        awayStatTime = itemView.findViewById(R.id.away_stat_time);
        awayStatUser = itemView.findViewById(R.id.away_stat_user);
        awayStatName = itemView.findViewById(R.id.away_stat_name);
        awayStatEmoji = itemView.findViewById(R.id.away_stat_emoji);

        itemView.setOnClickListener(view -> adapterListener.onItemClicked(model));
    }

    public void bind(Stat model) {
        this.model = model;
        boolean isHome = model.isHome();
        StatType statType = model.getStatType();

        TextView time = isHome ? homeStatTime : awayStatTime;
        TextView user = isHome ? homeStatUser : awayStatUser;
        TextView name = isHome ? homeStatName : awayStatName;
        TextView emoji = isHome ? homeStatEmoji : awayStatEmoji;

        time.setText(String.valueOf(model.getTime()));
        user.setText(model.getUser().getFirstName());
        name.setText(statType.getName());
        emoji.setText(statType.getEmoji());
    }
}
