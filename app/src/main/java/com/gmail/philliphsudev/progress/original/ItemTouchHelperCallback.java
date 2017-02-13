package com.gmail.philliphsudev.progress.original;

/**
 * Created by Phillip Hsu on 6/25/2015.
 *
 * A helper class to implement "swipe-to-dismiss" and "drag-and-drop" in RecyclerView items.
 */
public class ItemTouchHelperCallback /*extends ItemTouchHelper.Callback*/ {

    /*private SwipeDragListener mListener;

    public ItemTouchHelperCallback(RecyclerView.Adapter adapter) {
        if (!(adapter instanceof SwipeDragListener))
            throw new ClassCastException(
                    "Adapter used to create ItemTouchHelperCallback must implement SwipeDragListener");
        mListener = (SwipeDragListener) adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        if (mListener instanceof TaskAdapter)
            return ((TaskAdapter)mListener).isSortedByMyOrder();
        if (mListener instanceof TaskListAdapter) {
            // TaskLists can be dragged and dropped, except for the primary TaskLists.
            // This exception is handled in getMovementFlags()
            return true;
        }
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        if (mListener instanceof TaskListAdapter) {
            return false;
        }
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (mListener instanceof TaskListAdapter) {
            // Check if the item rep'd by the viewHolder is a primary TaskList
            TaskListAdapter adapter = (TaskListAdapter) mListener;
            TaskListAdapter.ViewHolder vh = (TaskListAdapter.ViewHolder) viewHolder;
            if (adapter.isPrimaryList(vh.getAdapterPosition())) {
                // Disable all movement directions for primary TaskLists
                return makeFlag(ItemTouchHelper.ACTION_STATE_IDLE,
                        ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END);
            }
        }
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
        // If drag and drop is not enabled then this method will never be called.
        Log.i("TouchHelper", "onMove()");
        mListener.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mListener.onItemSwipe(viewHolder.getAdapterPosition(), direction);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        // Reveal a colored rectangle that trails the view as it is swiped out of the parent's bound
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && isItemViewSwipeEnabled()) {
            // Get RecyclerView item from the ViewHolder
            View itemView = viewHolder.itemView;
            Paint p = new Paint();

            // If we get here, then the adapter is an instanceof TaskAdapter; anyway, only TaskAdapter supports swiping
            TaskAdapter taskAdapter = (TaskAdapter) mListener;

            if (dX > 0) {
                // Set color for positive displacement
                if (taskAdapter.isShowingCompletedTasks()) {
                    // Delete Task forever
                    p.setColor(Color.RED);
                } else {
                    // Complete the Task
                    p.setColor(Color.GREEN);
                }
                // Draw Rect with varying right side, equal to displacement dX
                c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                        (float) itemView.getBottom(), p);
            } else {
                // Set color for negative displacement
                if (taskAdapter.isShowingCompletedTasks()) {
                    p.setColor(Color.DKGRAY);
                } else {
                    p.setColor(Color.YELLOW);
                }
                // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
                c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                        (float) itemView.getRight(), (float) itemView.getBottom(), p);
            }
        }
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            // Highlight the item being dragged
            viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
        }
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        // This function is only being called after dragging (onSelectedChange)
        // and LTR swipe (onSwiped), but NOT RTL swipe (onSwiped)..

        Log.i(getClass().getSimpleName(), "Clearing view");
        super.clearView(recyclerView, viewHolder);
        viewHolder.itemView.setBackgroundColor(0);
    }

    @Override
    public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder current, RecyclerView.ViewHolder target) {
        Log.i("TouchHelper", "canDropOver()");
        if (mListener instanceof TaskListAdapter) {
            TaskListAdapter adapter = (TaskListAdapter) mListener;
            boolean isDraggingNonPrimaryListToPrimaryPos =
                    !adapter.isPrimaryList(current.getAdapterPosition())
                    && adapter.isPrimaryList(target.getAdapterPosition());
            if (isDraggingNonPrimaryListToPrimaryPos) {
                Log.i("TouchHelper", "Cannot drag custom list over primary lists.");
                return false;
            }
        }
        return true;
    }

    @Override
    public float getMoveThreshold(RecyclerView.ViewHolder viewHolder) {
        return super.getMoveThreshold(viewHolder);
    }*/
}
