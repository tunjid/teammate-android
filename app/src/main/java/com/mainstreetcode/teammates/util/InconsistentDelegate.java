package com.mainstreetcode.teammates.util;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;

import java.util.ArrayList;

public class InconsistentDelegate {

    public static LayoutManager wrap(RecyclerView.LayoutManager implementation, InconsistencyHandler inconsistencyHandler) {
        return new LayoutManager(implementation, inconsistencyHandler);
    }

    public static class LayoutManager extends RecyclerView.LayoutManager {

        private final RecyclerView.LayoutManager implementation;
        private final InconsistencyHandler inconsistencyHandler;

        private LayoutManager(RecyclerView.LayoutManager implementation, InconsistencyHandler inconsistencyHandler) {
            this.implementation = implementation;
            this.inconsistencyHandler = inconsistencyHandler;
        }

        @Override
        public void setMeasuredDimension(Rect childrenBounds, int wSpec, int hSpec) {
            implementation.setMeasuredDimension(childrenBounds, wSpec, hSpec);
        }

        @Override
        public void requestLayout() {
            implementation.requestLayout();
        }

        @Override
        public void assertInLayoutOrScroll(String message) {
            implementation.assertInLayoutOrScroll(message);
        }

        @Override
        public void assertNotInLayoutOrScroll(String message) {
            implementation.assertNotInLayoutOrScroll(message);
        }

        @Override
        public void setAutoMeasureEnabled(boolean enabled) {
            implementation.setAutoMeasureEnabled(enabled);
        }

        @Override
        public boolean isAutoMeasureEnabled() {
            return implementation.isAutoMeasureEnabled();
        }

        @Override
        public boolean supportsPredictiveItemAnimations() {
            return implementation.supportsPredictiveItemAnimations();
        }

        @Override
        public void collectAdjacentPrefetchPositions(int dx, int dy, RecyclerView.State state, LayoutPrefetchRegistry layoutPrefetchRegistry) {
            implementation.collectAdjacentPrefetchPositions(dx, dy, state, layoutPrefetchRegistry);
        }

        @Override
        public void collectInitialPrefetchPositions(int adapterItemCount, LayoutPrefetchRegistry layoutPrefetchRegistry) {
            implementation.collectInitialPrefetchPositions(adapterItemCount, layoutPrefetchRegistry);
        }

        @Override
        public boolean isAttachedToWindow() {
            return implementation.isAttachedToWindow();
        }

        @Override
        public void postOnAnimation(Runnable action) {
            implementation.postOnAnimation(action);
        }

        @Override
        public boolean removeCallbacks(Runnable action) {
            return implementation.removeCallbacks(action);
        }

        @Override
        @SuppressLint("MissingSuperCall")
        public void onAttachedToWindow(RecyclerView view) {
            implementation.onAttachedToWindow(view);
        }

        @Override
        @SuppressWarnings("deprecation")
        public void onDetachedFromWindow(RecyclerView view) {
            implementation.onDetachedFromWindow(view);
        }

        @Override
        public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
            implementation.onDetachedFromWindow(view, recycler);
        }

        @Override
        public boolean getClipToPadding() {
            return implementation.getClipToPadding();
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try { implementation.onLayoutChildren(recycler, state);}
            catch (IndexOutOfBoundsException e) {inconsistencyHandler.onInconsistencyDetected(e);}
        }

        @Override
        public void onLayoutCompleted(RecyclerView.State state) {
            implementation.onLayoutCompleted(state);
        }

        @Override
        public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
            return implementation.checkLayoutParams(lp);
        }

        @Override
        public RecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
            return implementation.generateLayoutParams(lp);
        }

        @Override
        public RecyclerView.LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
            return implementation.generateLayoutParams(c, attrs);
        }

        @Override
        public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
            return implementation.scrollHorizontallyBy(dx, recycler, state);
        }

        @Override
        public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
            return implementation.scrollVerticallyBy(dy, recycler, state);
        }

        @Override
        public boolean canScrollHorizontally() {
            return implementation.canScrollHorizontally();
        }

        @Override
        public boolean canScrollVertically() {
            return implementation.canScrollVertically();
        }

        @Override
        public void scrollToPosition(int position) {
            implementation.scrollToPosition(position);
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
            implementation.smoothScrollToPosition(recyclerView, state, position);
        }

        @Override
        public void startSmoothScroll(RecyclerView.SmoothScroller smoothScroller) {
            implementation.startSmoothScroll(smoothScroller);
        }

        @Override
        public boolean isSmoothScrolling() {
            return implementation.isSmoothScrolling();
        }

        @Override
        public int getLayoutDirection() {
            return implementation.getLayoutDirection();
        }

        @Override
        public void endAnimation(View view) {
            implementation.endAnimation(view);
        }

        @Override
        public void addDisappearingView(View child) {
            implementation.addDisappearingView(child);
        }

        @Override
        public void addDisappearingView(View child, int index) {
            implementation.addDisappearingView(child, index);
        }

        @Override
        public void addView(View child) {
            implementation.addView(child);
        }

        @Override
        public void addView(View child, int index) {
            implementation.addView(child, index);
        }

        @Override
        public void removeView(View child) {
            implementation.removeView(child);
        }

        @Override
        public void removeViewAt(int index) {
            implementation.removeViewAt(index);
        }

        @Override
        public void removeAllViews() {
            implementation.removeAllViews();
        }

        @Override
        public int getBaseline() {
            return implementation.getBaseline();
        }

        @Override
        public int getPosition(View view) {
            return implementation.getPosition(view);
        }

        @Override
        public int getItemViewType(View view) {
            return implementation.getItemViewType(view);
        }

        @Nullable
        @Override
        public View findContainingItemView(View view) {
            return implementation.findContainingItemView(view);
        }

        @Override
        public View findViewByPosition(int position) {
            return implementation.findViewByPosition(position);
        }

        @Override
        public void detachView(View child) {
            implementation.detachView(child);
        }

        @Override
        public void detachViewAt(int index) {
            implementation.detachViewAt(index);
        }

        @Override
        public void attachView(View child, int index, RecyclerView.LayoutParams lp) {
            implementation.attachView(child, index, lp);
        }

        @Override
        public void attachView(View child, int index) {
            implementation.attachView(child, index);
        }

        @Override
        public void attachView(View child) {
            implementation.attachView(child);
        }

        @Override
        public void removeDetachedView(View child) {
            implementation.removeDetachedView(child);
        }

        @Override
        public void moveView(int fromIndex, int toIndex) {
            implementation.moveView(fromIndex, toIndex);
        }

        @Override
        public void detachAndScrapView(View child, RecyclerView.Recycler recycler) {
            implementation.detachAndScrapView(child, recycler);
        }

        @Override
        public void detachAndScrapViewAt(int index, RecyclerView.Recycler recycler) {
            implementation.detachAndScrapViewAt(index, recycler);
        }

        @Override
        public void removeAndRecycleView(View child, RecyclerView.Recycler recycler) {
            implementation.removeAndRecycleView(child, recycler);
        }

        @Override
        public void removeAndRecycleViewAt(int index, RecyclerView.Recycler recycler) {
            implementation.removeAndRecycleViewAt(index, recycler);
        }

        @Override
        public int getChildCount() {
            return implementation.getChildCount();
        }

        @Override
        public View getChildAt(int index) {
            return implementation.getChildAt(index);
        }

        @Override
        public int getWidthMode() {
            return implementation.getWidthMode();
        }

        @Override
        public int getHeightMode() {
            return implementation.getHeightMode();
        }

        @Override
        public int getWidth() {
            return implementation.getWidth();
        }

        @Override
        public int getHeight() {
            return implementation.getHeight();
        }

        @Override
        public int getPaddingLeft() {
            return implementation.getPaddingLeft();
        }

        @Override
        public int getPaddingTop() {
            return implementation.getPaddingTop();
        }

        @Override
        public int getPaddingRight() {
            return implementation.getPaddingRight();
        }

        @Override
        public int getPaddingBottom() {
            return implementation.getPaddingBottom();
        }

        @Override
        public int getPaddingStart() {
            return implementation.getPaddingStart();
        }

        @Override
        public int getPaddingEnd() {
            return implementation.getPaddingEnd();
        }

        @Override
        public boolean isFocused() {
            return implementation.isFocused();
        }

        @Override
        public boolean hasFocus() {
            return implementation.hasFocus();
        }

        @Override
        public View getFocusedChild() {
            return implementation.getFocusedChild();
        }

        @Override
        public int getItemCount() {
            return implementation.getItemCount();
        }

        @Override
        public void offsetChildrenHorizontal(int dx) {
            implementation.offsetChildrenHorizontal(dx);
        }

        @Override
        public void offsetChildrenVertical(int dy) {
            implementation.offsetChildrenVertical(dy);
        }

        @Override
        public void ignoreView(View view) {
            implementation.ignoreView(view);
        }

        @Override
        public void stopIgnoringView(View view) {
            implementation.stopIgnoringView(view);
        }

        @Override
        public void detachAndScrapAttachedViews(RecyclerView.Recycler recycler) {
            implementation.detachAndScrapAttachedViews(recycler);
        }

        @Override
        public void measureChild(View child, int widthUsed, int heightUsed) {
            implementation.measureChild(child, widthUsed, heightUsed);
        }

        @Override
        public boolean isMeasurementCacheEnabled() {
            return implementation.isMeasurementCacheEnabled();
        }

        @Override
        public void setMeasurementCacheEnabled(boolean measurementCacheEnabled) {
            implementation.setMeasurementCacheEnabled(measurementCacheEnabled);
        }

        @Override
        public void measureChildWithMargins(View child, int widthUsed, int heightUsed) {
            implementation.measureChildWithMargins(child, widthUsed, heightUsed);
        }

        @Override
        public int getDecoratedMeasuredWidth(View child) {
            return implementation.getDecoratedMeasuredWidth(child);
        }

        @Override
        public int getDecoratedMeasuredHeight(View child) {
            return implementation.getDecoratedMeasuredHeight(child);
        }

        @Override
        public void layoutDecorated(View child, int left, int top, int right, int bottom) {
            implementation.layoutDecorated(child, left, top, right, bottom);
        }

        @Override
        public void layoutDecoratedWithMargins(View child, int left, int top, int right, int bottom) {
            implementation.layoutDecoratedWithMargins(child, left, top, right, bottom);
        }

        @Override
        public void getTransformedBoundingBox(View child, boolean includeDecorInsets, Rect out) {
            implementation.getTransformedBoundingBox(child, includeDecorInsets, out);
        }

        @Override
        public void getDecoratedBoundsWithMargins(View view, Rect outBounds) {
            implementation.getDecoratedBoundsWithMargins(view, outBounds);
        }

        @Override
        public int getDecoratedLeft(View child) {
            return implementation.getDecoratedLeft(child);
        }

        @Override
        public int getDecoratedTop(View child) {
            return implementation.getDecoratedTop(child);
        }

        @Override
        public int getDecoratedRight(View child) {
            return implementation.getDecoratedRight(child);
        }

        @Override
        public int getDecoratedBottom(View child) {
            return implementation.getDecoratedBottom(child);
        }

        @Override
        public void calculateItemDecorationsForChild(View child, Rect outRect) {
            implementation.calculateItemDecorationsForChild(child, outRect);
        }

        @Override
        public int getTopDecorationHeight(View child) {
            return implementation.getTopDecorationHeight(child);
        }

        @Override
        public int getBottomDecorationHeight(View child) {
            return implementation.getBottomDecorationHeight(child);
        }

        @Override
        public int getLeftDecorationWidth(View child) {
            return implementation.getLeftDecorationWidth(child);
        }

        @Override
        public int getRightDecorationWidth(View child) {
            return implementation.getRightDecorationWidth(child);
        }

        @Nullable
        @Override
        public View onFocusSearchFailed(View focused, int direction, RecyclerView.Recycler recycler, RecyclerView.State state) {
            return implementation.onFocusSearchFailed(focused, direction, recycler, state);
        }

        @Override
        public View onInterceptFocusSearch(View focused, int direction) {
            return implementation.onInterceptFocusSearch(focused, direction);
        }

        @Override
        public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate) {
            return implementation.requestChildRectangleOnScreen(parent, child, rect, immediate);
        }

        @Override
        public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate, boolean focusedChildVisible) {
            return implementation.requestChildRectangleOnScreen(parent, child, rect, immediate, focusedChildVisible);
        }

        @Override
        public boolean isViewPartiallyVisible(@NonNull View child, boolean completelyVisible, boolean acceptEndPointInclusion) {
            return implementation.isViewPartiallyVisible(child, completelyVisible, acceptEndPointInclusion);
        }

        @Override
        @SuppressWarnings("deprecation")
        public boolean onRequestChildFocus(RecyclerView parent, View child, View focused) {
            return implementation.onRequestChildFocus(parent, child, focused);
        }

        @Override
        public boolean onRequestChildFocus(RecyclerView parent, RecyclerView.State state, View child, View focused) {
            return implementation.onRequestChildFocus(parent, state, child, focused);
        }

        @Override
        public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
            implementation.onAdapterChanged(oldAdapter, newAdapter);
        }

        @Override
        public boolean onAddFocusables(RecyclerView recyclerView, ArrayList<View> views, int direction, int focusableMode) {
            return implementation.onAddFocusables(recyclerView, views, direction, focusableMode);
        }

        @Override
        public void onItemsChanged(RecyclerView recyclerView) {
            implementation.onItemsChanged(recyclerView);
        }

        @Override
        public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount) {
            implementation.onItemsAdded(recyclerView, positionStart, itemCount);
        }

        @Override
        public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
            implementation.onItemsRemoved(recyclerView, positionStart, itemCount);
        }

        @Override
        public void onItemsUpdated(RecyclerView recyclerView, int positionStart, int itemCount) {
            implementation.onItemsUpdated(recyclerView, positionStart, itemCount);
        }

        @Override
        public void onItemsUpdated(RecyclerView recyclerView, int positionStart, int itemCount, Object payload) {
            implementation.onItemsUpdated(recyclerView, positionStart, itemCount, payload);
        }

        @Override
        public void onItemsMoved(RecyclerView recyclerView, int from, int to, int itemCount) {
            implementation.onItemsMoved(recyclerView, from, to, itemCount);
        }

        @Override
        public int computeHorizontalScrollExtent(RecyclerView.State state) {
            return implementation.computeHorizontalScrollExtent(state);
        }

        @Override
        public int computeHorizontalScrollOffset(RecyclerView.State state) {
            return implementation.computeHorizontalScrollOffset(state);
        }

        @Override
        public int computeHorizontalScrollRange(RecyclerView.State state) {
            return implementation.computeHorizontalScrollRange(state);
        }

        @Override
        public int computeVerticalScrollExtent(RecyclerView.State state) {
            return implementation.computeVerticalScrollExtent(state);
        }

        @Override
        public int computeVerticalScrollOffset(RecyclerView.State state) {
            return implementation.computeVerticalScrollOffset(state);
        }

        @Override
        public int computeVerticalScrollRange(RecyclerView.State state) {
            return implementation.computeVerticalScrollRange(state);
        }

        @Override
        public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
            implementation.onMeasure(recycler, state, widthSpec, heightSpec);
        }

        @Override
        public void setMeasuredDimension(int widthSize, int heightSize) {
            implementation.setMeasuredDimension(widthSize, heightSize);
        }

        @Override
        public int getMinimumWidth() {
            return implementation.getMinimumWidth();
        }

        @Override
        public int getMinimumHeight() {
            return implementation.getMinimumHeight();
        }

        @Override
        public Parcelable onSaveInstanceState() {
            return implementation.onSaveInstanceState();
        }

        @Override
        public void onRestoreInstanceState(Parcelable state) {
            implementation.onRestoreInstanceState(state);
        }

        @Override
        public void onScrollStateChanged(int state) {
            implementation.onScrollStateChanged(state);
        }

        @Override
        public void removeAndRecycleAllViews(RecyclerView.Recycler recycler) {
            implementation.removeAndRecycleAllViews(recycler);
        }

        @Override
        public void onInitializeAccessibilityNodeInfo(RecyclerView.Recycler recycler, RecyclerView.State state, AccessibilityNodeInfoCompat info) {
            implementation.onInitializeAccessibilityNodeInfo(recycler, state, info);
        }

        @Override
        public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
            implementation.onInitializeAccessibilityEvent(event);
        }

        @Override
        public void onInitializeAccessibilityEvent(RecyclerView.Recycler recycler, RecyclerView.State state, AccessibilityEvent event) {
            implementation.onInitializeAccessibilityEvent(recycler, state, event);
        }

        @Override
        public void onInitializeAccessibilityNodeInfoForItem(RecyclerView.Recycler recycler, RecyclerView.State state, View host, AccessibilityNodeInfoCompat info) {
            implementation.onInitializeAccessibilityNodeInfoForItem(recycler, state, host, info);
        }

        @Override
        public void requestSimpleAnimationsInNextLayout() {
            implementation.requestSimpleAnimationsInNextLayout();
        }

        @Override
        public int getSelectionModeForAccessibility(RecyclerView.Recycler recycler, RecyclerView.State state) {
            return implementation.getSelectionModeForAccessibility(recycler, state);
        }

        @Override
        public int getRowCountForAccessibility(RecyclerView.Recycler recycler, RecyclerView.State state) {
            return implementation.getRowCountForAccessibility(recycler, state);
        }

        @Override
        public int getColumnCountForAccessibility(RecyclerView.Recycler recycler, RecyclerView.State state) {
            return implementation.getColumnCountForAccessibility(recycler, state);
        }

        @Override
        public boolean isLayoutHierarchical(RecyclerView.Recycler recycler, RecyclerView.State state) {
            return implementation.isLayoutHierarchical(recycler, state);
        }

        @Override
        public boolean performAccessibilityAction(RecyclerView.Recycler recycler, RecyclerView.State state, int action, Bundle args) {
            return implementation.performAccessibilityAction(recycler, state, action, args);
        }

        @Override
        public boolean performAccessibilityActionForItem(RecyclerView.Recycler recycler, RecyclerView.State state, View view, int action, Bundle args) {
            return implementation.performAccessibilityActionForItem(recycler, state, view, action, args);
        }

        @Override
        public RecyclerView.LayoutParams generateDefaultLayoutParams() {
            return implementation.generateDefaultLayoutParams();
        }
    }

    @FunctionalInterface
    public interface InconsistencyHandler {
        void onInconsistencyDetected(IndexOutOfBoundsException exception);
    }
}
