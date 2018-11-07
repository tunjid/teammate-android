package com.mainstreetcode.teammate.adapters.viewholders.input;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.material.textfield.TextInputLayout;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.util.ModelUtils;

import androidx.annotation.Nullable;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class InputViewHolder<T extends ImageWorkerFragment.ImagePickerListener> extends BaseViewHolder<T>
        implements
        TextWatcher {

    @Nullable
    TextInputStyle textInputStyle;

    private final EditText editText;
    private final ImageButton button;
    private final TextInputLayout inputLayout;

    public InputViewHolder(View itemView) {
        super(itemView);
        inputLayout = itemView.findViewById(R.id.input_layout);
        button = itemView.findViewById(R.id.button);
        editText = inputLayout.getEditText();
    }

    @Override protected void clear() {
        button.setOnClickListener(null);
        editText.setOnClickListener(null);
        editText.removeTextChangedListener(this);

        if (textInputStyle != null) textInputStyle.setViewHolder(null);
        textInputStyle = null;

        super.clear();
    }

    public void bind(TextInputStyle textInputStyle) {
        this.textInputStyle = textInputStyle;
        textInputStyle.setViewHolder(this);

        Item item = textInputStyle.getItem();
        boolean isSelector = textInputStyle.isSelector();

        inputLayout.setEnabled(textInputStyle.isEnabled());
        inputLayout.setHint(itemView.getContext().getString(item.getStringRes()));

        editText.setText(ModelUtils.processString(item.getValue()));
        editText.setInputType(item.getInputType());

        editText.setClickable(isSelector);
        editText.setFocusable(!isSelector);
        editText.setFocusableInTouchMode(!isSelector);
        editText.setOnClickListener(textInputStyle.textClickListener());
        button.setOnClickListener(textInputStyle.buttonClickListener());

        editText.removeTextChangedListener(this);
        if (!isSelector) editText.addTextChangedListener(this);

        checkForErrors();
        setClickableState();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {/* Nothing */}

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {/* Nothing */}

    @Override
    public void afterTextChanged(Editable editable) {
        if (textInputStyle == null || textInputStyle.isSelector()) return;
        textInputStyle.getItem().setValue(editable.toString());
        checkForErrors();
    }

    void updateText(CharSequence text) {
        editText.setText(text);
        checkForErrors();
    }

    private void checkForErrors() {
        if (textInputStyle == null) return;
        CharSequence errorMessage = textInputStyle.errorText();
        if (TextUtils.isEmpty(errorMessage)) editText.setError(null);
        else editText.setError(errorMessage);
    }

    private void setClickableState() {
        if (textInputStyle == null) return;

        int icon = textInputStyle.getIcon();
        int visibility = icon == 0 ? GONE : VISIBLE;

        button.setVisibility(visibility);
        if (icon != 0) button.setImageResource(icon);
    }
}
