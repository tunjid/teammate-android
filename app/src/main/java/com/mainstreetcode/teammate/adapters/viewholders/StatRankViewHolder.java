package com.mainstreetcode.teammate.adapters.viewholders;

import android.view.View;

import com.mainstreetcode.teammate.model.StatRank;
import com.mainstreetcode.teammate.util.ViewHolderUtil;

public class StatRankViewHolder extends ModelCardViewHolder<StatRank, ViewHolderUtil.SimpleAdapterListener<StatRank>>
        implements View.OnClickListener {

    public StatRankViewHolder(View itemView, ViewHolderUtil.SimpleAdapterListener<StatRank> adapterListener) {
        super(itemView, adapterListener);
        itemView.setOnClickListener(this);
    }

    public void bind(StatRank model) {
        super.bind(model);

        title.setText(model.getTitle());
        subtitle.setText(model.getSubtitle());
    }

    @Override
    public void onClick(View view) {
        adapterListener.onItemClicked(model);
    }
}
