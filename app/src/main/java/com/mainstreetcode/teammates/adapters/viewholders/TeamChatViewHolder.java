package com.mainstreetcode.teammates.adapters.viewholders;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamChatAdapter;
import com.mainstreetcode.teammates.model.Chat;
import com.mainstreetcode.teammates.model.Team;
import com.squareup.picasso.Picasso;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

/**
 * Viewholder for a {@link Team}
 */
public class TeamChatViewHolder extends BaseViewHolder<TeamChatAdapter.ChatAdapterListener> {

    private Chat item;
    private View leftGuide;
    private View rightGuide;
    private ImageView image;
    private TextView content;
    private TextView details;


    public TeamChatViewHolder(View itemView, TeamChatAdapter.ChatAdapterListener listener) {
        super(itemView, listener);
        image = itemView.findViewById(R.id.image);
        details = itemView.findViewById(R.id.details);
        content = itemView.findViewById(R.id.content);
        leftGuide = itemView.findViewById(R.id.left_guide);
        rightGuide = itemView.findViewById(R.id.right_guide);

        content.setOnClickListener(view -> adapterListener.onChatClicked(item));
    }

    public void bind(Chat item, boolean isSignedInUser, boolean showDetails) {

        this.item = item;
        boolean chatFailed = !item.isSuccessful();
        Context context = itemView.getContext();

        content.setText(item.getContent());
        itemView.setAlpha(item.isEmpty() ? 0.6F : 1F);
        image.setVisibility(isSignedInUser ? View.GONE : View.VISIBLE);
        details.setVisibility(showDetails || item.isEmpty() || chatFailed ? View.VISIBLE : View.GONE);

        if (!isSignedInUser && !TextUtils.isEmpty(item.getImageUrl())) {
            Picasso.with(context)
                    .load(item.getImageUrl())
                    .fit()
                    .centerCrop()
                    .into(image);
        }

        if (chatFailed) details.setText(R.string.chat_failed);
        else if (item.isEmpty()) details.setText(R.string.chat_sending);
        else if (showDetails) details.setText(getDetailsText(item, isSignedInUser, context));

        int margin = context.getResources().getDimensionPixelOffset(R.dimen.triple_margin);

        ConstraintLayout.LayoutParams leftParams = (ConstraintLayout.LayoutParams) leftGuide.getLayoutParams();
        ConstraintLayout.LayoutParams rightParams = (ConstraintLayout.LayoutParams) rightGuide.getLayoutParams();

        leftParams.horizontalBias = isSignedInUser ? 1F : 0F;
        leftParams.leftMargin = isSignedInUser ? margin : 0;
        rightParams.rightMargin = isSignedInUser ? 0 : margin;
    }

    private String getDetailsText(Chat item, boolean isSignedInUser, Context context) {
        String date = item.getCreatedDate();
        String firstName = item.getUser().getFirstName();
        return isSignedInUser ? date : context.getString(R.string.chat_details, firstName, date);
    }
}
