package com.mainstreetcode.teammate.adapters.viewholders;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Ad;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.squareup.picasso.Picasso;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * Viewholder for a {@link Team}
 */
public abstract class AdViewHolder<T extends Ad> extends InteractiveViewHolder<InteractiveAdapter.AdapterListener> {

    T ad;

    TextView title;
    TextView subtitle;
    ImageView thumbnail;

    AdViewHolder(View itemView, InteractiveAdapter.AdapterListener adapterListener) {
        super(itemView, adapterListener);
        title = itemView.findViewById(R.id.item_title);
        subtitle = itemView.findViewById(R.id.item_subtitle);
        thumbnail = itemView.findViewById(R.id.thumbnail);
    }

    @Nullable
    abstract String getImageUrl();

    public void bind(T ad) {
        this.ad = ad;
        setImageAspectRatio(ad);
        ViewHolderUtil.listenForLayout(thumbnail, () -> {
            String imageUrl = getImageUrl();

            if (!TextUtils.isEmpty(imageUrl)) Picasso.with(itemView.getContext())
                    .load(imageUrl)
                    .fit()
                    .centerCrop()
                    .into(thumbnail);
        });
    }

    private void setImageAspectRatio(Ad ad) {
        String aspectRatio = ad.getImageAspectRatio();
        if (aspectRatio != null) {
            ((ConstraintLayout.LayoutParams) thumbnail.getLayoutParams()).dimensionRatio = aspectRatio;
        }
    }
}
