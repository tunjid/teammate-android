package com.mainstreetcode.teammate.adapters.viewholders;

import android.view.View;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Stat;
import com.mainstreetcode.teammate.model.enums.StatType;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveViewHolder;


public class StatViewHolder extends InteractiveViewHolder<ViewHolderUtil.SimpleAdapterListener<Stat>> {

    protected Stat model;

    private TextView[] statTime;
    private TextView[] statUser;
    private TextView[] statName;
    private TextView[] statEmoji;

    public StatViewHolder(View itemView, ViewHolderUtil.SimpleAdapterListener<Stat> adapterListener) {
        super(itemView, adapterListener);
        statTime = new TextView[]{itemView.findViewById(R.id.home_stat_time), itemView.findViewById(R.id.away_stat_time)};
        statUser = new TextView[]{itemView.findViewById(R.id.home_stat_user), itemView.findViewById(R.id.away_stat_user)};
        statName = new TextView[]{itemView.findViewById(R.id.home_stat_name), itemView.findViewById(R.id.away_stat_name)};
        statEmoji = new TextView[]{itemView.findViewById(R.id.home_stat_emoji), itemView.findViewById(R.id.away_stat_emoji)};

        itemView.setOnClickListener(view -> adapterListener.onItemClicked(model));
    }

    public void bind(Stat model) {
        this.model = model;
        StatType statType = model.getStatType();

        setText(statTime, String.valueOf(model.getTime()));
        setText(statUser, model.getUser().getFirstName());
        setText(statName, statType.getName());
        setText(statEmoji, statType.getEmoji());
    }

    private void setText(TextView[] textViewPair, CharSequence text) {
        boolean isHome = model.isHome();
        int home = isHome ? 0 : 1;
        int away = isHome ? 1: 0;

        textViewPair[home].setText(text);
        textViewPair[away].setText("");
    }
}
