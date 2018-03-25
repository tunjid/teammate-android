package com.mainstreetcode.teammate.adapters.viewholders;

import android.support.v7.app.AlertDialog;
import android.view.View;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.util.Supplier;

import java.util.List;

/**
 * ViewHolder for selecting {@link com.mainstreetcode.teammate.model.Role}
 */
public class RoleSelectViewHolder extends ClickInputViewHolder
        implements View.OnClickListener {

    private final List<String> roles;

    public RoleSelectViewHolder(View itemView, List<String> roles, Supplier<Boolean> enabler) {
        super(itemView, enabler, () -> {});
        this.roles = roles;
    }

    @Override
    public void onClick(View view) {
        if (!isEnabled()) return;

        AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                .setTitle(R.string.choose_role)
                .setItems(roles.toArray(new String[roles.size()]), (dialogInterface, position) -> {
                    String role = roles.get(position);
                    item.setValue(role);
                    editText.setText(role);
                    editText.setError(null);
                })
                .create();

        dialog.setOnDismissListener(dialogInterface -> onDialogDismissed());
        dialog.show();
    }
}
