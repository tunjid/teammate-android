package com.mainstreetcode.teammate.adapters.viewholders;


import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;

import static com.mainstreetcode.teammate.adapters.viewholders.SnackBarUtils.findSuitableParent;
import static com.mainstreetcode.teammate.adapters.viewholders.SnackBarUtils.forceAnimation;

public class ChoiceBar extends BaseTransientBottomBar<ChoiceBar> {

    /**
     * Constructor for the transient bottom bar.
     *
     * @param parent   The parent for this transient bottom bar.
     * @param content  The content view for this transient bottom bar.
     * @param callback The content view callback for this transient bottom bar.
     */
    private ChoiceBar(ViewGroup parent, View content, android.support.design.snackbar.ContentViewCallback callback) {
        super(parent, content, callback);

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
        forceAnimation(choiceBar);

        return choiceBar;
    }

    public ChoiceBar setText(CharSequence message) {
        getView().<TextView>findViewById(R.id.text).setText(message);
        return this;
    }

    public ChoiceBar setPositiveText(CharSequence message) {
        getView().<TextView>findViewById(R.id.positive_button).setText(message);
        return this;
    }

    public ChoiceBar setNegativeText(CharSequence message) {
        getView().<TextView>findViewById(R.id.negative_button).setText(message);
        return this;
    }

    public ChoiceBar setPositiveClickListener(View.OnClickListener listener) {
        getView().findViewById(R.id.positive_button).setOnClickListener(view -> {
            listener.onClick(view);
            dismiss();
        });
        return this;
    }

    public ChoiceBar setNegativeClickListener(View.OnClickListener listener) {
        getView().findViewById(R.id.negative_button).setOnClickListener(view -> {
            listener.onClick(view);
            dismiss();
        });
        return this;
    }
}
