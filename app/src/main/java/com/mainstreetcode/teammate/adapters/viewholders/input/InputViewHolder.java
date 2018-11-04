package com.mainstreetcode.teammate.adapters.viewholders.input;

import android.content.res.ColorStateList;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.material.textfield.TextInputLayout;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.util.ModelUtils;

import androidx.core.content.ContextCompat;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class InputViewHolder<T extends ImageWorkerFragment.ImagePickerListener> extends BaseItemViewHolder<T>
        implements
        TextWatcher {

    private final EditText editText;
    private final ImageButton button;
    private final TextInputLayout inputLayout;

    public InputViewHolder(View itemView) {
        super(itemView);
        inputLayout = itemView.findViewById(R.id.input_layout);
        button = itemView.findViewById(R.id.button);
        editText = inputLayout.getEditText();
    }

    public void bind(TextInputStyle textInputStyle) {
        this.textInputStyle = textInputStyle;
        textInputStyle.setEditText(editText);

        Item item = textInputStyle.getItem();
        boolean isSelector = textInputStyle.isSelector();

        inputLayout.setEnabled(textInputStyle.isEnabled());
        inputLayout.setHint(itemView.getContext().getString(item.getStringRes()));

        editText.setText(ModelUtils.processString(item.getValue()));
        editText.setInputType(item.getInputType());

        editText.setFocusable(!isSelector);
        editText.setClickable(isSelector);
        editText.setOnClickListener(textInputStyle.textClickListener());
        button.setOnClickListener(textInputStyle.buttonClickListener());

        if (isSelector) editText.removeTextChangedListener(this);
        else editText.addTextChangedListener(this);

        checkForErrors();
        setClickableState();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {/* Nothing */}

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {/* Nothing */}

    @Override
    public void afterTextChanged(Editable editable) {
        if (textInputStyle.isSelector()) return;
        textInputStyle.getItem().setValue(editable.toString());
        checkForErrors();
    }

    private void checkForErrors() {
        CharSequence errorMessage = textInputStyle.errorText();
        if (TextUtils.isEmpty(errorMessage)) editText.setError(null);
        else editText.setError(errorMessage);
    }

    private void setClickableState() {
        int colorInt = ContextCompat.getColor(itemView.getContext(), textInputStyle.isEnabled() ? R.color.black : R.color.disabled_text);
        editText.setTextColor(ColorStateList.valueOf(colorInt));

        int icon = textInputStyle.getIcon();
        int visibility = icon == 0 ? GONE : VISIBLE;

        button.setVisibility(visibility);
        if (icon != 0) button.setImageResource(icon);
    }
}
