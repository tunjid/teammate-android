package com.mainstreetcode.teammate.adapters.viewholders;

import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.ImageView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.TournamentAdapter;
import com.mainstreetcode.teammate.model.Tournament;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.getTransitionName;


public class TournamentViewHolder extends ModelCardViewHolder<Tournament, TournamentAdapter.TournamentAdapterListener>
        implements View.OnClickListener {

    public TournamentViewHolder(View itemView, TournamentAdapter.TournamentAdapterListener adapterListener) {
        super(itemView, adapterListener);
        itemView.setOnClickListener(this);
    }

    public void bind(Tournament item) {
        super.bind(item);
        title.setText(item.getName());
        subtitle.setText(item.getDescription());

        ViewCompat.setTransitionName(itemView, getTransitionName(item, R.id.fragment_header_background));
        ViewCompat.setTransitionName(thumbnail, getTransitionName(item, R.id.fragment_header_thumbnail));
    }

    @Override
    public void onClick(View view) {
        adapterListener.onTournamentClicked(model);
    }

    public ImageView getImage() {
        return thumbnail;
    }
}
