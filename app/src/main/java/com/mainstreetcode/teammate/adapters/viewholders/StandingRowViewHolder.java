package com.mainstreetcode.teammate.adapters.viewholders;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Row;
import com.squareup.picasso.Picasso;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.List;

import static android.view.Gravity.CENTER;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


public class StandingRowViewHolder extends BaseViewHolder<BaseRecyclerViewAdapter.AdapterListener> {

    public TextView title;
    public TextView position;
    public ImageView thumbnail;
    private LinearLayout columns;

    public StandingRowViewHolder(View itemView, BaseRecyclerViewAdapter.AdapterListener adapterListener) {
        super(itemView, adapterListener);
        title = itemView.findViewById(R.id.item_title);
        position = itemView.findViewById(R.id.item_position);
        thumbnail = itemView.findViewById(R.id.thumbnail);
        columns = itemView.findViewById(R.id.item_row);
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
        for (int i = 0; i < count; i++) getItem(i).setText(columns.get(i));
    }

    private TextView getItem(int position) {
        int max = columns.getChildCount() - 1;
        if (position <= max) return (TextView) columns.getChildAt(position);

        TextView textView = new TextView(itemView.getContext());
        textView.setGravity(CENTER);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT, 1);
        columns.addView(textView, params);

        return textView;
    }
}
