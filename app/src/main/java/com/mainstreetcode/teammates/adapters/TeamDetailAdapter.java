package com.mainstreetcode.teammates.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.Team;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

/**
 * Adapter for {@link Team}
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

public class TeamDetailAdapter extends BaseRecyclerViewAdapter<TeamDetailAdapter.BaseTeamViewHolder, TeamDetailAdapter.TeamAdapterListener> {

    private final Team team;
    private final boolean isEditable;

    public TeamDetailAdapter(Team team, boolean isEditable, TeamAdapterListener listener) {
        super(listener);
        this.team = team;
        this.isEditable = isEditable;
    }

    @Override
    public BaseTeamViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();

        @LayoutRes int layoutRes = viewType == Team.HEADING
                ? R.layout.viewholder_team_header
                : viewType == Team.INPUT
                ? R.layout.viewholder_team_input
                : R.layout.viewholder_team_image;

        View itemView = LayoutInflater.from(context).inflate(layoutRes, viewGroup, false);

        switch (viewType) {
            case Team.HEADING:
                return new HeaderViewHolder(itemView, adapterListener);
            case Team.INPUT:
                return new InputViewHolder(itemView, adapterListener, isEditable);
            case Team.IMAGE:
                return new ImageViewHolder(itemView, adapterListener);
            default:
                throw new IllegalStateException("Unknown ViewHolder type");
        }
    }

    @Override
    public void onBindViewHolder(BaseTeamViewHolder baseTeamViewHolder, int i) {
        baseTeamViewHolder.bind(team.get(i));
    }

    @Override
    public int getItemCount() {
        return team.size();
    }

    @Override
    public int getItemViewType(int position) {
        return team.get(position).getItemType();
    }

    public interface TeamAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onTeamImageClicked(Team team);
    }

    static abstract class BaseTeamViewHolder extends BaseViewHolder<TeamAdapterListener> {

        Team.Item item;

        BaseTeamViewHolder(View itemView, TeamAdapterListener adapterListener) {
            super(itemView, adapterListener);
        }

        void bind(Team.Item item) {
            this.item = item;
        }
    }

    static class HeaderViewHolder extends BaseTeamViewHolder {
        TextView heading;

        HeaderViewHolder(View itemView, TeamAdapterListener adapterListener) {
            super(itemView, adapterListener);
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

        InputViewHolder(View itemView, TeamAdapterListener adapterListener, boolean isEditable) {
            super(itemView, adapterListener);
            inputLayout = itemView.findViewById(R.id.input_layout);
            inputLayout.setEnabled(isEditable);
        }

        @Override
        void bind(Team.Item item) {
            super.bind(item);
            EditText editText = inputLayout.getEditText();
            inputLayout.setHint(itemView.getContext().getString(item.getStringRes()));
            if (editText != null) editText.setText(item.getValue());

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
        }
    }

    static class ImageViewHolder extends BaseTeamViewHolder {

        ImageViewHolder(View itemView, TeamAdapterListener adapterListener) {
            super(itemView, adapterListener);
        }

    }
}
