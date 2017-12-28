package com.mainstreetcode.teammates.adapters.viewholders;

import android.support.v7.app.AlertDialog;
import android.view.View;

import com.mainstreetcode.teammates.R;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * ViewHolder for selecting {@link com.mainstreetcode.teammates.model.Role}
 */
public class RoleSelectViewHolder extends ClickInputViewHolder
        implements View.OnClickListener {

    private final List<String> roles;

    public RoleSelectViewHolder(View itemView, List<String> roles, Callable<Boolean> enabler) {
        super(itemView, enabler, () -> {});
        this.roles = roles;
    }

    @Override
    public void onClick(View view) {
        if(!isEnabled()) return;

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
