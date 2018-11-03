package com.mainstreetcode.teammate.adapters.viewholders;

import android.annotation.SuppressLint;
import androidx.arch.core.util.Function;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

public class DragDropViewHolder<T extends RecyclerView.ViewHolder> extends RecyclerView.ViewHolder {

    private final T holder;

    @SuppressLint("ClickableViewAccessibility")
    public DragDropViewHolder(T holder, Function<T, View> dragHandleFunction, DragListener dragListener) {
        super(holder.itemView);
        this.holder = holder;

        View dragHandle = dragHandleFunction.apply(holder);
        dragHandle.setVisibility(View.VISIBLE);
        dragHandle.setOnTouchListener(((view, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN)
                dragListener.onDragStarted(this);
            return false;
        }));
    }

    public T getHolder() { return holder; }

    public interface DragListener {
        void onDragStarted(RecyclerView.ViewHolder viewHolder);
    }
}
