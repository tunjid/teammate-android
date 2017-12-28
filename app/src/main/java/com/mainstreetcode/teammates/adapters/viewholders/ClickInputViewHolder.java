package com.mainstreetcode.teammates.adapters.viewholders;

import android.view.View;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.Item;

import java.util.concurrent.Callable;

/**
 * ViewHolder for selecting {@link com.mainstreetcode.teammates.model.Role}
 */
public class ClickInputViewHolder extends InputViewHolder
        implements View.OnClickListener {

    private final Runnable clickAction;

    public ClickInputViewHolder(View itemView, Callable<Boolean> enabler, Runnable clickAction) {
        super(itemView, enabler);
        this.clickAction = clickAction;
    }

    @Override
    public void bind(Item item) {
        super.bind(item);
        itemView.findViewById(R.id.click_view).setOnClickListener(isEnabled() ? this : null);
    }

    @Override
    public void onClick(View view) {
        clickAction.run();
    }
}
