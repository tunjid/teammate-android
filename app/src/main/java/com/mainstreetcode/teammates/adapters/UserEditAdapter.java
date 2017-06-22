package com.mainstreetcode.teammates.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.Item;
import com.mainstreetcode.teammates.model.User;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.List;

/**
 * Adapter for {@link User}
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

public class UserEditAdapter extends BaseRecyclerViewAdapter<UserEditAdapter.BaseUserViewHolder, BaseRecyclerViewAdapter.AdapterListener> {

    private static final int PADDING = 11;

    private final User team;
    private final List<String> roles;
    private final boolean isEditable;

    public UserEditAdapter(User team, List<String> roles, boolean isEditable) {
        this.team = team;
        this.roles = roles;
        this.isEditable = isEditable;
    }

    @Override
    public BaseUserViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();

        @LayoutRes int layoutRes = viewType == Item.INPUT || viewType == Item.ROLE
                ? R.layout.viewholder_team_input
                : viewType == Item.IMAGE
                ? R.layout.viewholder_team_image
                : R.layout.view_holder_padding;

        View itemView = LayoutInflater.from(context).inflate(layoutRes, viewGroup, false);

        switch (viewType) {
            case Item.INPUT:
                return new InputViewHolder(itemView, isEditable);
            case Item.ROLE:
                return new RoleViewHolder(itemView, roles);
            case Item.IMAGE:
                return new ImageViewHolder(itemView);
            default:
                return new BaseUserViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(BaseUserViewHolder baseUserViewHolder, int i) {
        if (i == team.size()) return;
        baseUserViewHolder.bind(team.get(i));
    }

    @Override
    public int getItemCount() {
        return team.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == team.size() ? PADDING : team.get(position).getItemType();
    }

    static class BaseUserViewHolder extends BaseViewHolder {
        Item item;

        BaseUserViewHolder(View itemView) {
            super(itemView);
        }

        void bind(Item item) {
            this.item = item;
        }
    }

    static class InputViewHolder extends BaseUserViewHolder
            implements
            TextWatcher {

        TextInputLayout inputLayout;
        EditText editText;
        TextView headerText;

        InputViewHolder(View itemView, boolean isEditable) {
            super(itemView);
            inputLayout = itemView.findViewById(R.id.input_layout);
            editText = inputLayout.getEditText();
            headerText = itemView.findViewById(R.id.header_name);

            inputLayout.setEnabled(isEditable);
            editText.addTextChangedListener(this);
        }

        @Override
        void bind(Item item) {
            super.bind(item);
            inputLayout.setHint(itemView.getContext().getString(item.getStringRes()));
            editText.setText(item.getValue());
            editText.setInputType(getAdapterPosition() == User.EMAIL_POSITION
                    ? InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                    : InputType.TYPE_CLASS_TEXT);

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

    static class ImageViewHolder extends BaseUserViewHolder {

        ImageViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class RoleViewHolder extends InputViewHolder
            implements View.OnClickListener {

        private final List<String> roles;

        RoleViewHolder(View itemView, List<String> roles) {
            super(itemView, false);
            this.roles = roles;
            itemView.findViewById(R.id.click_view).setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            builder.setTitle(R.string.choose_role);
            builder.setItems(roles.toArray(new String[roles.size()]), (dialog, position) -> {
                String role = roles.get(position);
                item.setValue(role);
                editText.setText(role);
                editText.setError(null);
            });
            builder.create().show();
        }
    }
}
