package com.gmail.philliphsudev.progress;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.philliphsudev.progress.model.TaskList;

import java.util.List;

/**
 * Created by Phillip Hsu on 7/10/2015.
 */
public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> implements SwipeDragListener {

    private List<TaskList> mTaskLists;

    public TaskListAdapter() {
        this(false);
    }

    public TaskListAdapter(boolean excludePrimaries) {
        mTaskLists = TaskListDb.lists(excludePrimaries);
        TaskListDb.registerTaskListAdapter(this);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_task_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // TODO set Task List color
        TaskList list = mTaskLists.get(position);
        holder.name.setText(list.getName());
    }

    @Override
    public int getItemCount() {
        return mTaskLists.size();
    }

    @Override
    public void onItemMove(int from, int to) {
        Log.i("TaskListAdapter", "onItemMove()");
        TaskListDb.moveLists(from, to);
    }

    @Override
    public void onItemSwipe(int position, int direction) {
        // Swiping is not supported in this adapter.
        throw new UnsupportedOperationException();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView color;
        public TextView name;

        public ViewHolder(View v) {
            super(v);

            this.color = (ImageView) v.findViewById(R.id.color);
            this.name  = (TextView)  v.findViewById(R.id.task_list);
        }
    }
}
