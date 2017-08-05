package com.mainstreetcode.teammates.adapters.viewholders;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.TeamChat;
import com.squareup.picasso.Picasso;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

/**
 * Viewholder for a {@link Team}
 */
public class TeamChatViewHolder extends BaseViewHolder {

    private TeamChat item;
    private View leftGuide;
    private View rightGuide;
    private ImageView image;
    private TextView content;
    private TextView details;


    public TeamChatViewHolder(View itemView) {
        super(itemView);
        image = itemView.findViewById(R.id.image);
        details = itemView.findViewById(R.id.details);
        content = itemView.findViewById(R.id.content);
        leftGuide = itemView.findViewById(R.id.left_guide);
        rightGuide = itemView.findViewById(R.id.right_guide);
    }

    public void bind(TeamChat item, boolean isSignedInUser, boolean showDetails) {

        this.item = item;
        Context context = itemView.getContext();

        content.setText(item.getContent());
        itemView.setAlpha(item.isEmpty() ? 0.6F : 1F);
        details.setVisibility(showDetails ? View.VISIBLE : View.GONE);

        if (!TextUtils.isEmpty(item.getImageUrl())) {
            Picasso.with(context)
                    .load(item.getImageUrl())
                    .fit()
                    .centerCrop()
                    .into(image);
        }
        if (showDetails) {
            String date = item.getCreated();
            String firstName = item.getUser().getFirstName();
            String value = isSignedInUser ? date : context.getString(R.string.chat_details, firstName, date);
            details.setText(value);
        }

        int margin = context.getResources().getDimensionPixelOffset(R.dimen.triple_margin);

        ConstraintLayout.LayoutParams leftParams = (ConstraintLayout.LayoutParams) leftGuide.getLayoutParams();
        ConstraintLayout.LayoutParams rightParams = (ConstraintLayout.LayoutParams) rightGuide.getLayoutParams();

        leftParams.horizontalBias = isSignedInUser ? 1F : 0F;
        leftParams.leftMargin = isSignedInUser ? margin : 0;
        rightParams.rightMargin = isSignedInUser ? 0 : margin;
    }
}
