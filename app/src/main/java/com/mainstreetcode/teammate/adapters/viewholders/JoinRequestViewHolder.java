/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.adapters.viewholders;

import android.content.Context;
import android.view.View;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.TeamMemberAdapter;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.User;

import static androidx.core.view.ViewCompat.setTransitionName;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.getTransitionName;

public class JoinRequestViewHolder extends ModelCardViewHolder<JoinRequest, TeamMemberAdapter.UserAdapterListener>
        implements View.OnClickListener {

    public JoinRequestViewHolder(View itemView, TeamMemberAdapter.UserAdapterListener adapterListener) {
        super(itemView, adapterListener);
        itemView.setOnClickListener(this);
    }

    @Override
    public void bind(JoinRequest model) {
        super.bind(model);

        User item = model.getUser();
        Context context = itemView.getContext();

        title.setText(item.getFirstName());
        subtitle.setText(model.isTeamApproved() && !model.isUserApproved()
                ? context.getString(R.string.user_invited, model.getPosition().getName())
                : context.getString(R.string.user_requests_join, model.getPosition().getName()));

        setTransitionName(itemView, getTransitionName(model, R.id.fragment_header_background));
        setTransitionName(thumbnail, getTransitionName(model, R.id.fragment_header_thumbnail));
    }

    @Override
    public void onClick(View view) {
        adapterListener.onJoinRequestClicked(model);
    }
}
