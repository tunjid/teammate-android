package com.mainstreetcode.teammate.adapters.viewholders;

import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.GameAdapter;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.util.AppBarListener;
import com.squareup.picasso.Picasso;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.THUMBNAIL_SIZE;


public class GameViewHolder extends BaseViewHolder<GameAdapter.AdapterListener> {

    private static final float ONE_F = 1F;

    private final int animationPadding;

    private Game model;
    private View highlight;
    private TextView ended;
    private TextView score;
    private TextView date;
    private TextView homeText;
    private TextView awayText;
    private ImageView homeThumbnail;
    private ImageView awayThumbnail;

    public GameViewHolder(View itemView, GameAdapter.AdapterListener adapterListener) {
        super(itemView, adapterListener);
        date = itemView.findViewById(R.id.date);
        score = itemView.findViewById(R.id.score);
        ended = itemView.findViewById(R.id.ended);
        homeText = itemView.findViewById(R.id.home);
        awayText = itemView.findViewById(R.id.away);
        highlight = itemView.findViewById(R.id.highlight);
        homeThumbnail = itemView.findViewById(R.id.home_thumbnail);
        awayThumbnail = itemView.findViewById(R.id.away_thumbnail);

        animationPadding = itemView.getResources().getDimensionPixelSize(R.dimen.quarter_margin);
        itemView.setOnClickListener(view -> adapterListener.onGameClicked(model));
    }

    public void bind(Game model) {
        this.model = model;
        Competitor home = model.getHome();
        Competitor away = model.getAway();
        Competitor winner = model.getWinner();

        date.setText(model.getDate());
        score.setText(model.getScore());
        homeText.setText(home.getName());
        awayText.setText(away.getName());
        ended.setVisibility(model.isEnded() ? View.VISIBLE : View.INVISIBLE);
        highlight.setVisibility(model.isEnded() ? View.INVISIBLE : View.VISIBLE);
        homeText.setTypeface(homeText.getTypeface(), home.equals(winner) ? Typeface.BOLD : Typeface.NORMAL);
        awayText.setTypeface(awayText.getTypeface(), away.equals(winner) ? Typeface.BOLD : Typeface.NORMAL);

        String homeUrl = home.getImageUrl();
        String awayUrl = away.getImageUrl();

        if (TextUtils.isEmpty(homeUrl)) homeThumbnail.setImageResource(R.color.dark_grey);
        else Picasso.with(itemView.getContext()).load(homeUrl)
                .resize(THUMBNAIL_SIZE, THUMBNAIL_SIZE).centerInside().into(homeThumbnail);

        if (TextUtils.isEmpty(awayUrl)) awayThumbnail.setImageResource(R.color.dark_grey);
        else Picasso.with(itemView.getContext()).load(awayUrl).
                resize(THUMBNAIL_SIZE, THUMBNAIL_SIZE).centerInside().into(awayThumbnail);
    }

    public void animate(AppBarListener.OffsetProps props) {
        int offset = props.getOffset();
        float fraction = props.getFraction();
        float scale = ONE_F - (fraction * 2.25F);
        int drop = Math.min(offset, (int) (homeText.getY() - homeThumbnail.getY()) - animationPadding);

        homeThumbnail.setAlpha(scale);
        awayThumbnail.setAlpha(scale);
        homeThumbnail.setScaleX(scale);
        awayThumbnail.setScaleX(scale);
        homeThumbnail.setScaleY(scale);
        awayThumbnail.setScaleY(scale);
        homeThumbnail.setTranslationY(drop);
        awayThumbnail.setTranslationY(drop);
    }
}
