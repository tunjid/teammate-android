package com.mainstreetcode.teammates.adapters.viewholders;

import android.view.View;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamEditAdapter;

/**
 * ViewHolder for selecting {@link com.mainstreetcode.teammates.model.Role}
 */
public class ClickInputViewHolder extends InputViewHolder<TeamEditAdapter.TeamEditAdapterListener>
        implements View.OnClickListener {

    public ClickInputViewHolder(View itemView, TeamEditAdapter.TeamEditAdapterListener listener) {
        super(itemView, false);
        this.adapterListener = listener;
        itemView.findViewById(R.id.click_view).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        adapterListener.onAddressClicked();
    }
}
