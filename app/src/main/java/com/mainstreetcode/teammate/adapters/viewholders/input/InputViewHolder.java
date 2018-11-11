package com.mainstreetcode.teammate.adapters.viewholders.input;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Item;

import java.util.Objects;

import static android.text.TextUtils.isEmpty;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.listenForLayout;

public class InputViewHolder<T extends ImageWorkerFragment.ImagePickerListener> extends BaseViewHolder<T>
        implements
        TextWatcher {

    private int lastLineCount = 1;

    private final TextView hint;
    private final EditText text;
    private final ImageButton button;

    @Nullable TextInputStyle textInputStyle;

    public InputViewHolder(View itemView) {
        super(itemView);
        hint = itemView.findViewById(R.id.hint);
        text = itemView.findViewById(R.id.input);
        button = itemView.findViewById(R.id.button);
        text.setOnFocusChangeListener((v, hasFocus) -> scaleHint(!hasFocus && isEmpty(text.getText())));
    }

    @Override protected void clear() {
        button.setOnClickListener(null);
        text.setOnClickListener(null);
        text.removeTextChangedListener(this);

        if (textInputStyle != null) textInputStyle.setViewHolder(null);
        textInputStyle = null;

        super.clear();
    }

    public void bind(TextInputStyle inputStyle) {
        this.textInputStyle = inputStyle;
        inputStyle.setViewHolder(this);

        Item item = inputStyle.getItem();

        int newInputType = item.getInputType();
        int oldInputType = text.getInputType();

        boolean isEditable = inputStyle.isEditable();
        boolean isSelector = inputStyle.isSelector();

        boolean isEnabled = text.isEnabled();
        boolean isClickable = text.isClickable();
        boolean isFocusable = text.isFocusable();
        boolean isFocusableInTouchMode = text.isFocusableInTouchMode();

        CharSequence newValue = item.getValue().toString();
        CharSequence oldValue = text.getText().toString();

        CharSequence oldHint = hint.getText();
        CharSequence newHint = itemView.getContext().getString(item.getStringRes());

        if (isEnabled != isEditable) text.setEnabled(isEditable);
        if (isClickable != isSelector) text.setClickable(isSelector);
        if (isFocusable == isSelector) text.setFocusable(!isSelector);
        if (isFocusableInTouchMode == isSelector) text.setFocusableInTouchMode(!isSelector);

        if (!Objects.equals(oldHint, newHint)) hint.setText(newHint);
        if (!Objects.equals(oldValue, newValue)) text.setText(newValue);
        if (oldInputType != newInputType) text.setInputType(newInputType);

        text.setOnClickListener(inputStyle.textClickListener());
        button.setOnClickListener(inputStyle.buttonClickListener());

        text.removeTextChangedListener(this);
        if (!isSelector) text.addTextChangedListener(this);

        checkForErrors();
        setClickableState();
        listenForLayout(hint, () -> scaleHint(isEmpty(text.getText())));
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {/* Nothing */}

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {/* Nothing */}

    @Override
    public void afterTextChanged(Editable editable) {
        int currentLineCount = text.getLineCount();
        if (lastLineCount != currentLineCount) listenForLayout(hint, () -> scaleHint(false));
        lastLineCount = currentLineCount;

        if (textInputStyle == null || textInputStyle.isSelector()) return;
        textInputStyle.getItem().setValue(editable.toString());
        checkForErrors();
    }

    void updateText(CharSequence text) {
        this.text.setText(text);
        checkForErrors();
    }

    private void checkForErrors() {
        if (textInputStyle == null) return;
        CharSequence errorMessage = textInputStyle.errorText();
        if (isEmpty(errorMessage)) text.setError(null);
        else text.setError(errorMessage);
    }

    private void setClickableState() {
        if (textInputStyle == null) return;

        int icon = textInputStyle.getIcon();
        int visibility = icon == 0 ? GONE : VISIBLE;

        button.setVisibility(visibility);
        if (icon != 0) button.setImageResource(icon);
    }

    private void scaleHint(boolean grow) {
        float scale = grow ? 1F : 0.8F;

        float translationX = grow ? 0 : -((hint.getWidth() - (0.8F * hint.getWidth())) * 0.5F);
        float translationY = grow ? 0 : -((itemView.getHeight() - hint.getHeight()) * (text.getLineCount() > 1 ? 0.58F : 0.5F));

        hint.animate()
                .scaleX(scale)
                .scaleY(scale)
                .translationX(translationX)
                .translationY(translationY)
                .setDuration(200).start();
    }
}
