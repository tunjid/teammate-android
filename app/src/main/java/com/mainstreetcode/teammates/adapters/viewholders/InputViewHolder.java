package com.mainstreetcode.teammates.adapters.viewholders;

import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.Item;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;

/**
 * Viewholder for editing simple text fields for an {@link Item}
 */
public class InputViewHolder extends BaseItemViewHolder
        implements
        TextWatcher {

    EditText editText;
    private TextView headerText;
    private TextInputLayout inputLayout;

    public InputViewHolder(View itemView, boolean isEditable) {
        super(itemView);
        inputLayout = itemView.findViewById(R.id.input_layout);
        editText = inputLayout.getEditText();
        headerText = itemView.findViewById(R.id.header_name);

        inputLayout.setEnabled(isEditable);
        editText.addTextChangedListener(this);
    }

    @Override
    public void bind(Item item) {
        super.bind(item);
        inputLayout.setHint(itemView.getContext().getString(item.getStringRes()));
        editText.setText(item.getValue());

        int position = getAdapterPosition();
        Class itemizedClass = item.getItemizedClass();

        editText.setInputType(itemizedClass.equals(Team.class) && position == Team.ZIP_POSITION
                ? InputType.TYPE_CLASS_NUMBER
                : itemizedClass.equals(User.class) && position == User.EMAIL_POSITION
                ? InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        if (item.getHeaderStringRes() != 0) {
            headerText.setText(item.getHeaderStringRes());
            headerText.setVisibility(View.VISIBLE);
        }
        else {
            headerText.setVisibility(View.GONE);
        }

        checkForErrors();
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
        if (TextUtils.isEmpty(editText.getText())) {
            editText.setError(editText.getContext().getString(R.string.team_invalid_empty_field));
        }
    }
}
