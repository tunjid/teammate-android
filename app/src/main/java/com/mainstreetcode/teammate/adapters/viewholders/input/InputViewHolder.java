package com.mainstreetcode.teammate.adapters.viewholders.input;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Item;

import java.util.Objects;

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

    public void bind(TextInputStyle inputStyle) {
        this.textInputStyle = inputStyle;
        inputStyle.setViewHolder(this);

        Item item = inputStyle.getItem();

        int newInputType = item.getInputType();
        int oldInputType = editText.getInputType();

        boolean isEditable = inputStyle.isEditable();
        boolean isSelector = inputStyle.isSelector();

        boolean isEnabled = editText.isEnabled();
        boolean isClickable = editText.isClickable();
        boolean isFocusable = editText.isFocusable();
        boolean isFocusableInTouchMode = editText.isFocusableInTouchMode();

        CharSequence newValue = item.getValue().toString();
        CharSequence oldValue = editText.getText().toString();

        CharSequence oldHint = inputLayout.getHint();
        CharSequence newHint = itemView.getContext().getString(item.getStringRes());

        if (isEnabled != isEditable) inputLayout.setEnabled(isEditable);
        if (isClickable != isSelector) editText.setClickable(isSelector);
        if (isFocusable == isSelector) editText.setFocusable(!isSelector);
        if (isFocusableInTouchMode == isSelector) editText.setFocusableInTouchMode(!isSelector);

        if (!Objects.equals(oldValue, newValue)) editText.setText(newValue);
        if (!Objects.equals(oldHint, newHint)) inputLayout.setHint(newHint);
        if (oldInputType != newInputType) editText.setInputType(newInputType);

        editText.setOnClickListener(inputStyle.textClickListener());
        button.setOnClickListener(inputStyle.buttonClickListener());

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
