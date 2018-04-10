package com.mainstreetcode.teammate.adapters.viewholders;

import android.content.res.ColorStateList;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.Supplier;

/**
 * Viewholder for editing simple text fields for an {@link Item}
 */
public class InputViewHolder<T extends ImageWorkerFragment.ImagePickerListener> extends BaseItemViewHolder<T>
        implements
        TextWatcher {

    EditText editText;
    @Nullable
    private final TextView headerText;
    private final TextInputLayout inputLayout;
    private final Supplier<Boolean> errorChecker;
    private final Supplier<Boolean> enabler;

    public InputViewHolder(View itemView, Supplier<Boolean> enabler, @Nullable Supplier<Boolean> errorChecker) {
        super(itemView);
        this.enabler = enabler;
        this.errorChecker = errorChecker == null ? this::hasText : errorChecker;
        inputLayout = itemView.findViewById(R.id.input_layout);
        editText = inputLayout.getEditText();
        headerText = itemView.findViewById(R.id.header_name);

        inputLayout.setEnabled(isEnabled());
        editText.addTextChangedListener(this);
    }

    public InputViewHolder(View itemView, Supplier<Boolean> enabler) {
        this(itemView, enabler, null);
    }

    @Override
    public void bind(Item item) {
        super.bind(item);
        inputLayout.setEnabled(isEnabled());
        inputLayout.setHint(itemView.getContext().getString(item.getStringRes()));
        editText.setText(item.getValue());

        int position = getAdapterPosition();
        Class itemizedClass = item.getItemizedObject().getClass();

        editText.setInputType(item.getItemType() == Item.NUMBER
                ? InputType.TYPE_CLASS_NUMBER
                : itemizedClass.equals(User.class) && position == User.EMAIL_POSITION
                ? InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        if (item.getHeaderStringRes() != 0 && headerText != null) {
            headerText.setText(item.getHeaderStringRes());
            headerText.setVisibility(View.VISIBLE);
        }
        else if (headerText != null) {
            headerText.setVisibility(View.GONE);
        }

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
