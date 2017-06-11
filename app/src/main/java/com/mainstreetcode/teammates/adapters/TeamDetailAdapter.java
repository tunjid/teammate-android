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
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.List;

/**
 * Adapter for {@link Team}
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

public class TeamDetailAdapter extends BaseRecyclerViewAdapter<TeamDetailAdapter.BaseTeamViewHolder, BaseRecyclerViewAdapter.AdapterListener> {

    private static final int PADDING = 11;

    private final Team team;
    private final List<Role> roles;
    private final boolean isEditable;

    public TeamDetailAdapter(Team team, List<Role> roles, boolean isEditable) {
        this.team = team;
        this.roles = roles;
        this.isEditable = isEditable;
    }

    @Override
    public BaseTeamViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();

        @LayoutRes int layoutRes = viewType == Team.HEADING
                ? R.layout.viewholder_team_header
                : viewType == Team.INPUT || viewType == Team.ROLE
                ? R.layout.viewholder_team_input
                : viewType == Team.IMAGE
                ? R.layout.viewholder_team_image
                : R.layout.view_holder_padding;

        View itemView = LayoutInflater.from(context).inflate(layoutRes, viewGroup, false);

        switch (viewType) {
            case Team.HEADING:
                return new HeaderViewHolder(itemView);
            case Team.INPUT:
                return new InputViewHolder(itemView, isEditable);
            case Team.ROLE:
                return new RoleViewHolder(itemView, roles);
            case Team.IMAGE:
                return new ImageViewHolder(itemView);
            default:
                return new BaseTeamViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(BaseTeamViewHolder baseTeamViewHolder, int i) {
        if (i == team.size()) return;
        baseTeamViewHolder.bind(team.get(i));
    }

    @Override
    public int getItemCount() {
        return team.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == team.size() ? PADDING : team.get(position).getItemType();
    }

    static  class BaseTeamViewHolder extends BaseViewHolder {
        Team.Item item;

        BaseTeamViewHolder(View itemView) {
            super(itemView);
        }

        void bind(Team.Item item) {
            this.item = item;
        }
    }

    static class HeaderViewHolder extends BaseTeamViewHolder {
        TextView heading;

        HeaderViewHolder(View itemView) {
            super(itemView);
            heading = itemView.findViewById(R.id.header_name);
        }

        @Override
        void bind(Team.Item item) {
            super.bind(item);
            heading.setText(item.getStringRes());
        }
    }

    static class InputViewHolder extends BaseTeamViewHolder
            implements
            TextWatcher {

        TextInputLayout inputLayout;
        EditText editText;

        InputViewHolder(View itemView, boolean isEditable) {
            super(itemView);
            inputLayout = itemView.findViewById(R.id.input_layout);
            editText = inputLayout.getEditText();

            inputLayout.setEnabled(isEditable);
            editText.addTextChangedListener(this);
        }

        @Override
        void bind(Team.Item item) {
            super.bind(item);
            inputLayout.setHint(itemView.getContext().getString(item.getStringRes()));
            editText.setText(item.getValue());
            editText.setInputType(getAdapterPosition() == Team.ZIP_POSITION
                    ? InputType.TYPE_CLASS_NUMBER
                    : InputType.TYPE_CLASS_TEXT);

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

    static class ImageViewHolder extends BaseTeamViewHolder {

        ImageViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class RoleViewHolder extends InputViewHolder
            implements View.OnClickListener {

        private final List<Role> roles;

        RoleViewHolder(View itemView, List<Role> roles) {
            super(itemView, false);
            this.roles = roles;
            itemView.findViewById(R.id.click_view).setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {

            int size = roles.size();
            final CharSequence[] roleNames = new CharSequence[size];
            for (int i = 0; i < size; i++) roleNames[i] = roles.get(i).getName();

            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            builder.setTitle(R.string.choose_role);
            builder.setItems(roleNames, (dialog, position) -> {
                Role role = roles.get(position);
                item.setValue(role.getName());
                editText.setText(role.getName());
                editText.setError(null);
            });
            builder.create().show();
        }
    }
}
