package com.mainstreetcode.teammate.adapters.viewholders;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.CompetitorAdapter;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Team;

/**
 * Viewholder for a {@link Team}
 */
public class CompetitorViewHolder extends ModelCardViewHolder<Competitor, CompetitorAdapter.AdapterListener>{

    private View dragHandle;

    @SuppressLint("ClickableViewAccessibility")
    public CompetitorViewHolder(View itemView, CompetitorAdapter.AdapterListener adapterListener) {
        super(itemView, adapterListener);
        dragHandle = itemView.findViewById(R.id.drag_handle);
        dragHandle.setOnTouchListener(((view, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN)
                adapterListener.onDragStarted(this);
            return false;
        }));
    }

    public void bind(Competitor model) {
        super.bind(model);
        title.setText(model.getName());
        dragHandle.setVisibility(model.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
