package com.mainstreetcode.teammate.adapters.viewholders;


import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;

import static android.support.design.widget.BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_ACTION;
import static android.support.design.widget.BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_TIMEOUT;
import static com.mainstreetcode.teammate.adapters.viewholders.SnackBarUtils.findSuitableParent;

public class ChoiceBar extends BaseTransientBottomBar<ChoiceBar> {

    private TextView positiveButton;
    private TextView negativeButton;

    /**
     * Constructor for the transient bottom bar.
     *
     * @param parent   The parent for this transient bottom bar.
     * @param content  The content view for this transient bottom bar.
     * @param callback The content view callback for this transient bottom bar.
     */
    private ChoiceBar(ViewGroup parent, View content, android.support.design.snackbar.ContentViewCallback callback) {
        super(parent, content, callback);

        positiveButton = content.findViewById(R.id.positive_button);
        negativeButton = content.findViewById(R.id.negative_button);

        // Remove the default insets applied that account for the keyboard showing up
        // since it's handled by us
        View wrapper = (View) content.getParent();
        ViewCompat.setOnApplyWindowInsetsListener(wrapper, (view, insets) -> insets);
    }

    public static ChoiceBar make(@NonNull View view, int duration) {
        final ViewGroup parent = findSuitableParent(view);
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        final View content = inflater.inflate(R.layout.snackbar_choice, parent, false);
        final android.support.design.snackbar.ContentViewCallback viewCallback = new SnackBarUtils.SnackbarAnimationCallback(content);
        final ChoiceBar choiceBar = new ChoiceBar(parent, content, viewCallback);

        choiceBar.setDuration(duration);
        return choiceBar;
    }

    @Override
    public void show() {
        if (TextUtils.isEmpty(positiveButton.getText())) positiveButton.setVisibility(View.GONE);
        if (TextUtils.isEmpty(negativeButton.getText())) negativeButton.setVisibility(View.GONE);
        positiveButton = negativeButton = null;
        super.show();
    }

    public void dismissAsTimeout() { dispatchDismiss(DISMISS_EVENT_TIMEOUT); }

    public ChoiceBar setText(CharSequence message) {
        getView().<TextView>findViewById(R.id.text).setText(message);
        return this;
    }

    public ChoiceBar setPositiveText(CharSequence message) {
        positiveButton.setText(message);
        return this;
    }

    public ChoiceBar setNegativeText(CharSequence message) {
        negativeButton.setText(message);
        return this;
    }

    public ChoiceBar setPositiveClickListener(View.OnClickListener listener) {
        positiveButton.setOnClickListener(view -> {
            listener.onClick(view);
            dispatchDismiss(DISMISS_EVENT_ACTION);
        });
        return this;
    }

    public ChoiceBar setNegativeClickListener(View.OnClickListener listener) {
        negativeButton.setOnClickListener(view -> {
            listener.onClick(view);
            dispatchDismiss(DISMISS_EVENT_ACTION);
        });
        return this;
    }
}
