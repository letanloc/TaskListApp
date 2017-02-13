package com.gmail.philliphsudev.progress;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.philliphsudev.progress.model.CompletedTaskList;
import com.gmail.philliphsudev.progress.model.CustomTaskList;
import com.gmail.philliphsudev.progress.model.Task;
import com.gmail.philliphsudev.progress.model.TaskList;

import java.util.Comparator;
import java.util.List;

/**
 * Created by Phillip Hsu on 9/4/2015.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> implements SwipeDragListener {

    private TaskList mTaskList;
    private List<Task> mTasks;
    private boolean mIsShowingCompletedTasks;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // View components comprising this ViewHolder
        public TextView mTaskListName, mTaskName, mTime;
        public ImageView mUrgentMarker;

        // Constructor
        public ViewHolder(View v) {
            super(v);
            // Initialize view components within the ViewHolder
            mTaskListName  =  (TextView)  v.findViewById(R.id.task_list);
            mTaskName      =  (TextView)  v.findViewById(R.id.task_name);
            mTime          =  (TextView)  v.findViewById(R.id.time);
            mUrgentMarker  =  (ImageView) v.findViewById(R.id.important_marker);
        }
    }

    public TaskAdapter(TaskList list) {
        //mTaskList = list;
        // Show completed tasks from the get go if task list is 'Completed', otherwise show imcomplete tasks
        // for all other task lists
        //toggleTasks(TaskListDb.isCompleted(list));
        changeTaskList(list);
    }

    @Override
    public TaskAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view by inflating the appropriate layout
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Task task = mTasks.get(position);
        holder.mTaskListName.setText(TaskListDb.getList(task.getTaskListIndex()).getName());
        holder.mTaskName.setText(task.getName());
        holder.mTime.setText(CalendarUtils.getTimeString(task.dueOn()));
        if (task.isImportant()) {
            holder.mUrgentMarker.setVisibility(View.VISIBLE);
        } else {
            holder.mUrgentMarker.setVisibility(View.INVISIBLE);
        }
    }

    // Return the size of the dataset (invoked by layout manager)
    @Override
    public int getItemCount() {
        return mTasks.size();
    }

    @Override
    public int getItemViewType(int position) {
        return determineItemViewType();
    }

    @Override
    public void onItemMove(int from, int to) {
        mTaskList.getTask(from).setMyPosition(to);
        mTaskList.getTask(to).setMyPosition(from);
        // The touch helper will only call this method if the working TaskList is a custom one,
        // so casting to CustomTaskList will succeed.
        ((CustomTaskList) mTaskList).swapTasks(from, to);
        notifyItemMoved(from, to);
    }

    @Override
    public void onItemSwipe(int position, int direction) {
        if (direction == ItemTouchHelper.END) {
            if (!isShowingCompletedTasks()) {
                mTaskList.completeTask(position);
                // The Task in this TaskList has been removed, and added to Completed Tasks.
                // Notice you don't need to notify of any insertions, since Completed Tasks aren't in view.
                notifyItemRemoved(position);
            } else {
                ((CustomTaskList) mTaskList).deleteCompletedTask(position);
                notifyItemRemoved(position);
            }
        } else if (direction == ItemTouchHelper.START) {
            if (!isShowingCompletedTasks()) {
                // TODO: Secondary action is "snooze"
            } else {
                ((CustomTaskList) mTaskList).restoreTask(position);
                notifyItemRemoved(position);
            }
        }
    }

    public void changeTaskList(TaskList list) {
        mTaskList = list;
        // Should always show the list of (incomplete) tasks on changing TaskLists, unless
        // the TaskList is 'Completed'
        if (mTaskList instanceof CompletedTaskList) {
            toggleTasks(true);
        } else {
            toggleTasks(false);
        }
    }

    public void toggleTasks(boolean showCompleted) {
        if (showCompleted) {
            mIsShowingCompletedTasks = true;
            if (mTaskList instanceof CompletedTaskList) {
                mTasks = mTaskList.getTasks();
            } else {
                try {
                    mTasks = ((CustomTaskList) mTaskList).getCompletedTasks();
                } catch (ClassCastException e) {
                    throw new ClassCastException(
                            "Could not cast mTaskList to CustomTaskList. Are you calling toggleTasks() on a PrimaryTaskList?");
                }
            }
        } else {
            mIsShowingCompletedTasks = false;
            mTasks = mTaskList.getTasks();
        }
        notifyDataSetChanged();
    }

    public boolean isShowingCompletedTasks() {
        return mIsShowingCompletedTasks;
    }

    public TaskList currentTaskList() {
        return mTaskList;
    }

    public boolean isSortedByMyOrder() {
        return mTaskList.getComparator() == ComparatorUtils.MY_ORDER;
    }

    private int determineItemViewType() {
        Comparator comp = mTaskList.getComparator();
        if (comp == null || comp == ComparatorUtils.IMPORTANCE)
            return R.layout.item_task;
        if (comp == ComparatorUtils.NAME) {
            // TODO
        }

        // TODO item view type for Completed tasks, so you can have different swipe directions
        // doing different things in the task list and in the completed tasks
        return R.layout.item_task;
    }
}
