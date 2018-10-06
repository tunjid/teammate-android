package com.mainstreetcode.teammate.adapters.viewholders;

import android.app.Activity;
import android.arch.core.util.Function;
import android.view.View;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseActivity;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.util.Supplier;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.getActivity;

/**
 * ViewHolder for selecting {@link com.mainstreetcode.teammate.model.Role}
 */
public class ClickInputViewHolder extends InputViewHolder
        implements View.OnClickListener {

    private final Runnable clickAction;

    public ClickInputViewHolder(View itemView, Supplier<Boolean> enabler, Runnable clickAction) {
        super(itemView, enabler);
        editText.removeTextChangedListener(this);
        this.clickAction = clickAction;
    }

    public ClickInputViewHolder(View itemView, Supplier<Boolean> enabler, Runnable clickAction, Function<CharSequence, CharSequence> errorChecker) {
        super(itemView, enabler, errorChecker);
        editText.removeTextChangedListener(this);
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
