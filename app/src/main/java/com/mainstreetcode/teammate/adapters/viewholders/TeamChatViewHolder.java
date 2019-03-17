package com.mainstreetcode.teammate.adapters.viewholders;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.TeamChatAdapter;
import com.mainstreetcode.teammate.model.Chat;
import com.mainstreetcode.teammate.model.Team;
import com.squareup.picasso.Picasso;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

/**
 * Viewholder for a {@link Team}
 */
public class TeamChatViewHolder extends InteractiveViewHolder<TeamChatAdapter.ChatAdapterListener> {

    private Chat item;
    private View space;
    private ImageView image;
    private TextView content;
    private TextView details;


    public TeamChatViewHolder(View itemView, TeamChatAdapter.ChatAdapterListener listener) {
        super(itemView, listener);
        image = itemView.findViewById(R.id.image);
        space = itemView.findViewById(R.id.mid_guide);
        details = itemView.findViewById(R.id.details);
        content = itemView.findViewById(R.id.content);
        content.setOnClickListener(view -> adapterListener.onChatClicked(item));
    }

    public void bind(Chat item, boolean isSignedInUser, boolean showDetails, boolean showPicture) {

        this.item = item;
        Context context = itemView.getContext();
        boolean chatFailed = !item.isSuccessful();

        content.setText(item.getContent());
        itemView.setAlpha(item.isEmpty() ? 0.6F : 1F);
        image.setVisibility(showPicture ? View.VISIBLE : View.GONE);
        space.setVisibility(showPicture ? View.VISIBLE : View.GONE);
        details.setVisibility(showDetails || item.isEmpty() || chatFailed ? View.VISIBLE : View.GONE);
        content.setBackgroundResource(isSignedInUser ? R.drawable.bg_chat_box : R.drawable.bg_chat_box_alt);

        if (!isSignedInUser && !TextUtils.isEmpty(item.getImageUrl()))
            Picasso.get().load(item.getImageUrl()).fit().centerCrop().into(image);

        if (chatFailed) details.setText(R.string.chat_failed);
        else if (item.isEmpty()) details.setText(R.string.chat_sending);
        else if (showDetails) details.setText(getDetailsText(item, isSignedInUser, context));

        ConstraintLayout.LayoutParams detailsParams = (ConstraintLayout.LayoutParams) details.getLayoutParams();
        ConstraintLayout.LayoutParams leftParams = (ConstraintLayout.LayoutParams) image.getLayoutParams();

        leftParams.horizontalBias = detailsParams.horizontalBias = isSignedInUser ? 1F : 0F;
    }

    private CharSequence getDetailsText(Chat item, boolean isSignedInUser, Context context) {
        CharSequence date = item.getCreatedDate();
        CharSequence firstName = item.getUser().getFirstName();
        return isSignedInUser ? date : context.getString(R.string.chat_details, firstName, date);
    }
}
