package com.gmail.philliphsudev.progress.original;

/**
 * Created by Phillip Hsu on 7/10/2015.
 */
public class TaskListAdapter {/*extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> implements SwipeDragListener {
    private static final int TYPE_CUSTOM_LIST = 0;
    private static final int TYPE_PRIMARY_LIST = 1; // Primary TaskList view type, i.e. non-draggable

    private List<TaskList> sTaskLists;

    private boolean mExcludePrimaryLists;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView color;
        public TextView name;

        public ViewHolder(View v) {
            super(v);

            this.color = (ImageView) v.findViewById(R.id.color);
            this.name  = (TextView)  v.findViewById(R.id.task_list);
        }
    }

    public TaskListAdapter() {
        this(false);
    }

    public TaskListAdapter(boolean excludePrimaryLists) {
        if (excludePrimaryLists) {
            mExcludePrimaryLists = excludePrimaryLists;
            // Make a sublist of the List<TaskList> starting from 3, inclusive, to size(), exclusive.
            int offset = TaskListDb.NUM_PRIMARY_LISTS;
            sTaskLists = TaskListDb.lists().subList(offset, TaskListDb.size());
        } else {
            sTaskLists = TaskListDb.lists();
        }
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
        TaskList list = sTaskLists.get(position);
        holder.name.setText(list.getName());
    }

    @Override
    public int getItemCount() {
        return sTaskLists.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mExcludePrimaryLists)
            return TYPE_CUSTOM_LIST;
        return position > 2 ? TYPE_CUSTOM_LIST : TYPE_PRIMARY_LIST;
    }

    @Override
    public void onItemMove(int from, int to) {
        Log.i("TaskListAdapter", "onItemMove()");
        for (Task task : TaskListDb.getList(from).getTasks())
            task.setTaskListIndex(to);
        for (Task task : TaskListDb.getList(to).getTasks())
            task.setTaskListIndex(from);
        TaskListDb.getList(from).setIndex(to);
        TaskListDb.getList(to).setIndex(from);
        TaskListDb.moveLists(from, to);
    }

    @Override
    public void onItemSwipe(int position, int direction) {
        // Swiping is not supported in this adapter.
    }

    // Determines if the specified position corresponds to a primary TaskList.
    public boolean isPrimaryList(int at) {
        return getItemViewType(at) == TYPE_PRIMARY_LIST;
    }*/
}
