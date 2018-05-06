package com.mainstreetcode.teammate.adapters.viewholders;

import android.content.res.ColorStateList;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.util.Supplier;

import static android.view.View.*;

/**
 * Viewholder for editing simple text fields for an {@link Item}
 */
public class InputViewHolder<T extends ImageWorkerFragment.ImagePickerListener> extends BaseItemViewHolder<T>
        implements
        TextWatcher {

    EditText editText;
    private final ImageButton button;
    private final TextInputLayout inputLayout;
    private final Supplier<Boolean> errorChecker;
    private final Supplier<Boolean> enabler;

    public InputViewHolder(View itemView, Supplier<Boolean> enabler, @Nullable Supplier<Boolean> errorChecker) {
        super(itemView);
        this.enabler = enabler;
        this.errorChecker = errorChecker == null ? this::hasText : errorChecker;
        inputLayout = itemView.findViewById(R.id.input_layout);
        button = itemView.findViewById(R.id.button);
        editText = inputLayout.getEditText();

        inputLayout.setEnabled(isEnabled());
        editText.addTextChangedListener(this);
    }

    public InputViewHolder(View itemView, Supplier<Boolean> enabler) {
        this(itemView, enabler, null);
    }

    public InputViewHolder setButtonRunnable(@DrawableRes int icon, Runnable clickRunnable) {
        button.setVisibility(VISIBLE);
        button.setImageResource(icon);
        button.setOnClickListener(clicked -> clickRunnable.run());
        return this;
    }

    @Override
    public void bind(Item item) {
        super.bind(item);
        inputLayout.setEnabled(isEnabled());
        inputLayout.setHint(itemView.getContext().getString(item.getStringRes()));
        editText.setText(item.getValue());
        editText.setInputType(item.getInputType());

        checkForErrors();
        setClickableState();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        // Nothing
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        // Nothing
    }

    @Override
    public void afterTextChanged(Editable editable) {
        item.setValue(editable.toString());
        checkForErrors();
    }

    private void checkForErrors() {
        if (!errorChecker.get()) editText.setError(null);
        else editText.setError(editText.getContext().getString(R.string.team_invalid_empty_field));
    }

    private void setClickableState() {
        int colorInt = ContextCompat.getColor(itemView.getContext(), isEnabled() ? R.color.black : R.color.light_grey);
        editText.setTextColor(ColorStateList.valueOf(colorInt));
    }

    private boolean hasText() {
        return TextUtils.isEmpty(editText.getText());
    }

    protected boolean isEnabled() {
        try {return enabler.get();}
        catch (Exception e) {return false;}
    }
}
