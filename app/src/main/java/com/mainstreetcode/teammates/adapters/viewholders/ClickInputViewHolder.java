package com.mainstreetcode.teammates.adapters.viewholders;

import android.app.Activity;
import android.view.View;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.baseclasses.TeammatesBaseActivity;
import com.mainstreetcode.teammates.model.Item;
import com.mainstreetcode.teammates.util.Supplier;

import static com.mainstreetcode.teammates.util.ViewHolderUtil.getActivity;

/**
 * ViewHolder for selecting {@link com.mainstreetcode.teammates.model.Role}
 */
public class ClickInputViewHolder extends InputViewHolder
        implements View.OnClickListener {

    private final Runnable clickAction;

    public ClickInputViewHolder(View itemView, Supplier<Boolean> enabler, Runnable clickAction) {
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

    void onDialogDismissed() {
        Activity activity = getActivity(this);
        if (activity != null && activity instanceof TeammatesBaseActivity)
            ((TeammatesBaseActivity) activity).onDialogDismissed();
    }
}
