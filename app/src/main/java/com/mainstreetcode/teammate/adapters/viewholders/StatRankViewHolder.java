package com.mainstreetcode.teammate.adapters.viewholders;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.StatRank;
import com.mainstreetcode.teammate.util.ViewHolderUtil;

public class StatRankViewHolder extends ModelCardViewHolder<StatRank, ViewHolderUtil.SimpleAdapterListener<StatRank>>
        implements View.OnClickListener {

    private TextView count;
    private ImageView inset;

    public StatRankViewHolder(View itemView, ViewHolderUtil.SimpleAdapterListener<StatRank> adapterListener) {
        super(itemView, adapterListener);
        itemView.setOnClickListener(this);
        count = itemView.findViewById(R.id.item_position);
        inset = itemView.findViewById(R.id.inset);
    }

    public void bind(StatRank model) {
        super.bind(model);

        count.setText(model.getCount());
        title.setText(model.getTitle());
        subtitle.setText(model.getSubtitle());

        String imageUrl = model.getInset();

        if (!TextUtils.isEmpty(imageUrl)) load(imageUrl, inset);
    }

    @Override
    public void onClick(View view) {
        adapterListener.onItemClicked(model);
    }

    @Override
    public boolean isThumbnail() { return false; }
}
