package com.mainstreetcode.teammate.adapters.viewholders;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.StandingsAdapter;
import com.mainstreetcode.teammate.model.Row;
import com.mainstreetcode.teammate.util.SyncedScrollView;
import com.squareup.picasso.Picasso;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.List;

import static android.view.Gravity.CENTER;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;


public class StandingRowViewHolder extends BaseViewHolder<StandingsAdapter.AdapterListener> {

    public TextView title;
    public TextView position;
    public ImageView thumbnail;
    private LinearLayout columns;

    public StandingRowViewHolder(View itemView, StandingsAdapter.AdapterListener adapterListener) {
        super(itemView, adapterListener);
        title = itemView.findViewById(R.id.item_title);
        position = itemView.findViewById(R.id.item_position);
        thumbnail = itemView.findViewById(R.id.thumbnail);
        columns = itemView.findViewById(R.id.item_row);

        SyncedScrollView scrollView = itemView.findViewById(R.id.synced_scrollview);
        scrollView.setHorizontalScrollBarEnabled(false);

        adapterListener.addScrollNotifier(scrollView);
    }

    public void bind(Row model) {
        title.setText(model.getName());

        int adapterPosition = getAdapterPosition();
        position.setText(String.valueOf(adapterPosition + 1));

        String imageUrl = model.getImageUrl();
        if (TextUtils.isEmpty(imageUrl)) thumbnail.setImageResource(R.color.dark_grey);
        else Picasso.with(itemView.getContext()).load(imageUrl).fit().centerCrop().into(thumbnail);
    }

    public void bindColumns(List<String> columns) {
        int count = columns.size();
        this.columns.setWeightSum(count);
        for (int i = 0; i < count; i++) getItem(i).setText(columns.get(i));
    }

    private TextView getItem(int position) {
        int max = columns.getChildCount() - 1;
        int margin = itemView.getResources().getDimensionPixelSize(R.dimen.double_margin);
        if (position <= max) return (TextView) columns.getChildAt(position);

        TextView textView = new TextView(itemView.getContext());
        textView.setGravity(CENTER);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(margin, MATCH_PARENT);
        columns.addView(textView, params);

        return textView;
    }
}
