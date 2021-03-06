package com.beloo.widget.chipslayoutmanager.layouter;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.View;

class LTRDownLayouter extends AbstractLayouter {

    private boolean isPurged;

    private LTRDownLayouter(Builder builder) {
        super(builder);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    void onPreLayout() {
        if (!rowViews.isEmpty()) {
            //todo this isn't great place for that. Should be refactored somehow
            if (!isPurged) {
                isPurged = true;
                getCacheStorage().purgeCacheFromPosition(getLayoutManager().getPosition(rowViews.get(0).second));
            }

            //cache only when go down
            getCacheStorage().storeRow(rowViews);
        }
    }

    @Override
    void onAfterLayout() {
        //go to next row, increase top coordinate, reset left
        viewLeft = getCanvasLeftBorder();
        rowTop = rowBottom;
    }

    @Override
    boolean isAttachedViewFromNewRow(View view) {

        int topOfCurrentView = getLayoutManager().getDecoratedTop(view);
        int leftOfCurrentView = getLayoutManager().getDecoratedLeft(view);

        return rowBottom <= topOfCurrentView
                && leftOfCurrentView < viewLeft;

    }

    @Override
    public boolean canNotBePlacedInCurrentRow() {
        return super.canNotBePlacedInCurrentRow()
                || (getCurrentViewPosition() != 0 && getBreaker().isItemBreakRow(getCurrentViewPosition() - 1))
                || (viewLeft > getCanvasLeftBorder() && viewLeft + getCurrentViewWidth() > getCanvasRightBorder());
    }

    @Override
    AbstractPositionIterator createPositionIterator() {
        return new IncrementalPositionIterator(getLayoutManager().getItemCount());
    }

    @Override
    Rect createViewRect(View view) {
        Rect viewRect = new Rect(viewLeft, rowTop, viewLeft + getCurrentViewWidth(), rowTop + getCurrentViewHeight());

        viewLeft = viewRect.right;
        rowBottom = Math.max(rowBottom, viewRect.bottom);
        return viewRect;
    }

    @Override
    public void onInterceptAttachView(View view) {
        rowTop = getLayoutManager().getDecoratedTop(view);
        viewLeft = getLayoutManager().getDecoratedRight(view);
        rowBottom = Math.max(rowBottom, getLayoutManager().getDecoratedBottom(view));
    }

    public static final class Builder extends AbstractLayouter.Builder {
        private Builder() {
        }

        @NonNull
        public LTRDownLayouter build() {
            return new LTRDownLayouter(this);
        }
    }
}
