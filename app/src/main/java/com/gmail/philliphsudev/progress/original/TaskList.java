package com.gmail.philliphsudev.progress.original;

/**
 * Created by Phillip Hsu on 7/10/2015.
 */
public class TaskList /*implements Parcelable*/ {

    /*public enum MenuOptions { SORT, VIEW_COMPLETED_TASKS, EDIT_TASK_LIST,
        SORT_BY_DUE_DATE,
        SORT_BY_IMPORTANCE,
        SORT_BY_NAME,
        SORT_BY_ORDER_CREATED,
        SORT_BY_TASK_LIST,
        SORT_BY_MY_ORDER
    }

    // Order and description of indices for any int[] returned by the CRUD methods, meant for
    // updating the TaskAdapter in TaskListFragment of item insertions/removals/etc.
    public static final int INDEX_OF_TASK_IN_ACTUAL = 0;
    public static final int INDEX_OF_TASK_IN_ALL = 1;
    public static final int INDEX_OF_TASK_IN_IMPORTANT = 2;
    public static final int INDEX_OF_TASK_IN_COMPLETED = 3;

    private String mName;
    private int mColor;
    private int mIndex = -1; // The position of this TaskList in all list views, as ordered by the user
    private List<Task> mTasks;
    private Comparator<Task> mComparator = ComparatorUtils.DUE_DATE; // Note: each TaskList instance can have its own sorting mode
    private List<Task> mCompletedTasks;
    private final EnumMap<MenuOptions, Boolean> mSupportedMenuOptions = new EnumMap<>(MenuOptions.class);

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeInt(mColor);
        dest.writeTypedList(mTasks);
        dest.writeInt(mIndex);
        dest.writeTypedList(mCompletedTasks);
        dest.writeInt(ComparatorUtils.intKey(mComparator));
        for (boolean b : mSupportedMenuOptions.values()) {
            dest.writeInt(b ? 1 : 0);
        }
    }

    public static final Parcelable.Creator<TaskList> CREATOR = new Parcelable.Creator<TaskList>() {
        @Override
        public TaskList createFromParcel(Parcel source) {
            return new TaskList(source);
        }

        @Override
        public TaskList[] newArray(int size) {
            return new TaskList[size];
        }
    };

    private TaskList(Parcel in) {
        mName = in.readString();
        mColor = in.readInt();
        mTasks = in.createTypedArrayList(Task.CREATOR);
        mIndex = in.readInt();
        mCompletedTasks = in.createTypedArrayList(Task.CREATOR);
        mComparator = ComparatorUtils.valueOf(in.readInt());
        for (MenuOptions option : MenuOptions.values()) {
            mSupportedMenuOptions.put(option, in.readInt() != 0);
        }
    }

    public TaskList() {
        this("");
    }

    public TaskList(String name) {
        this(name, 0);
    }

    public TaskList(String name, int color) {
        mName = name;
        mColor = color;
        mTasks = new ArrayList<>();
        mCompletedTasks = new ArrayList<>();

        for (MenuOptions option : MenuOptions.values()) {
            // By default, all TaskLists will have all options supported, except for 'Sort by task list'
            // which is only supported by the 'All' primary TaskList
            if (option == MenuOptions.SORT_BY_TASK_LIST)
                mSupportedMenuOptions.put(option, false);
            else
                mSupportedMenuOptions.put(option, true);
        }
    }

    public TaskList(TaskList rhs) {
        mName = rhs.mName;
        mColor = rhs.mColor;
        mTasks = new ArrayList<>();
        mCompletedTasks = new ArrayList<>();
        mIndex = rhs.mIndex;
        for (Task task : rhs.mTasks) {
            mTasks.add(new Task(task));
        }
        for (Task task : rhs.mCompletedTasks) {
            mCompletedTasks.add(new Task(task));
        }
        mComparator = rhs.mComparator;
        for (MenuOptions option : MenuOptions.values()) {
            mSupportedMenuOptions.put(option, rhs.mSupportedMenuOptions.get(option));
        }
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
    }

    public List<Task> getTasks() {
        // Prevent the returned instance from being modified by outside classes
        // All changes to the task list have to be done inside this class
        return mTasks;
    }

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int index) {
        this.mIndex = index;
        for (Task t : mTasks) {
            t.setTaskListIndex(mIndex);
        }
    }

    public List<Task> getCompletedTasks() {
        return mCompletedTasks;
    }

    public Comparator<Task> getComparator() {
        return mComparator;
    }

    public void setComparator(Comparator<Task> comparator) {
        mComparator = comparator;
        Collections.sort(mTasks, mComparator);
    }

    public int size() {
        return mTasks.size();
    }

    public int[] addTask(Task task) {
        int[] indices = new int[4];
        if (!isPrimaryTaskList()) {
            task.setTaskListIndex(mIndex); // Indicate that the Task belongs to this TaskList's index.
            task.setMyPosition(size() - 1); // Tracks the insertion order
            if (task.isCompleted()) {
                this.addCompletedTask(task);
                // This always returns 0....
                indices[INDEX_OF_TASK_IN_COMPLETED] = TaskListDb.addTaskToCompleted(task);
                // Return here b/c the task doesn't need to go anywhere else
                return indices;
            }
            indices[INDEX_OF_TASK_IN_ALL] = TaskListDb.addTaskToAll(task); // Recursively calls addTask() to add the task to 'All'
            if (task.isImportant()) {
                indices[INDEX_OF_TASK_IN_IMPORTANT] = TaskListDb.addTaskToImportant(task);
            }
        }
        mTasks.add(task); // Add to the end of the list. Always returns true.
        Collections.sort(mTasks, mComparator);
        indices[INDEX_OF_TASK_IN_ACTUAL] = mTasks.indexOf(task); // Where this task ended up after the sort.
        return indices;
    }

    public int replaceTask(int position, Task task) {

        ////////////////////////////////////////LEFT OFF HERE////////////////////////////////////////
        Task curr = getTask(position);
        if (curr.isCompleted()) {
            if (task.isCompleted()) {
                if (TaskListDb.isCompleted(this)) {
                    TaskListDb.replaceTaskInCompleted(position, task);
                    TaskList actual = TaskListDb.getList(curr.getTaskListIndex());
                    actual.mCompletedTasks.set(actual.mCompletedTasks.indexOf(curr), task);
                } else {
                    TaskListDb.replaceTaskInCompleted(curr, task);
                    mCompletedTasks.set(position, task);
                }
            } else {
                if (!TaskListDb.isCompleted(this)) {
                    deleteCompletedTask(position);
                    addTask(task); // Add the task to the TaskList, and any primary TaskLists if applicable
                } else {
                    // Delete the completed task from the actual TaskList and remove it from 'Completed'
                    // ie. deleteCompletedTask(indexOf(curr)) or something.........
                    // Also add the task to the actual TaskList ie. actual.addTask(task);
                }
            }
        }
        /////////////////////////////////////////////////////////////////////////////////////////////

        //

        ////////////// Everything below here is the original code //////////////////////////////////

        if (task.isCompleted()) {
            this.deleteTask(position);
            this.addCompletedTask(task);
            TaskListDb.addTaskToCompleted(task);
            // Return here b/c the task doesn't need to go anywhere else
            return -1; // index not necessary
        }
        Task prev = mTasks.set(position, task);
        Collections.sort(mTasks, mComparator);
        if (!TaskListDb.isAll(this)) {
            TaskListDb.replaceTaskInAll(prev, task);
        }
        if (!TaskListDb.isImportant(this) && prev.isImportant()) {
            if (task.isImportant()) {
                // TODO TaskListDb.replaceTaskInImportant(prev, task);
            } else {
                TaskListDb.removeTaskFromImportant(prev);
            }
        }
        if (isPrimaryTaskList()) {
            TaskList actual = TaskListDb.getList(prev.getTaskListIndex());
            actual.mTasks.set(actual.mTasks.indexOf(prev), task);
            Collections.sort(actual.mTasks, actual.mComparator);
        }
        return mTasks.indexOf(task); // Where this task ended up after sort.
    }

    public Task deleteTask(int i) {
        Task task = mTasks.remove(i);
        if (!TaskListDb.isAll(this)) {
            TaskListDb.removeTaskFromAll(task);
        }
        if (!TaskListDb.isImportant(this) && task.isImportant()) {
            TaskListDb.removeTaskFromImportant(task);
        }
        if (isPrimaryTaskList()) {
            TaskList actual = TaskListDb.getList(task.getTaskListIndex());
            actual.mTasks.remove(task);
        }
        return task; // May want to make a Snackbar announcing the deleted Task's name.
    }

    public void completeTask(int at) {
        Task task = mTasks.remove(at);
        task.setCompleted(true);
        if (!TaskListDb.isAll(this)) {
            TaskListDb.removeTaskFromAll(task);
        }
        if (!TaskListDb.isImportant(this) && task.isImportant()) {
            TaskListDb.removeTaskFromImportant(task);
        }
        if (!isPrimaryTaskList()) {
            mCompletedTasks.add(task);
            Collections.sort(mCompletedTasks, ComparatorUtils.DATE_COMPLETED);
        } else {
            TaskList actual = TaskListDb.getList(task.getTaskListIndex());
            actual.mTasks.remove(task);
            actual.mCompletedTasks.add(task);
            Collections.sort(actual.mCompletedTasks, ComparatorUtils.DATE_COMPLETED);
        }
        TaskListDb.addTaskToCompleted(task);
        // Don't need to return an index for notifying any adapters of changes because
        // you can never be viewing the completed tasks when you're in the task list.
    }

    public void restoreTask(int at) {
        // Restore the completed Task back into the task list. Returns the resulting index so
        // you can notify the adapter of the insertion.
        //
        // *********
        // ACTUALLY, by the same reasoning in the footnote of completeTask(), you don't need to
        // return an index. You can only be in the completed tasks when you're restoring Tasks.
        //
        // Couldn't use addTask() since we want to preserve the value of the Task's mMyPosition
        // from when it was still uncompleted
        // addTask(mCompletedTasks.remove(at));
        Task task = mCompletedTasks.remove(at);
        task.setCompleted(false);
        mTasks.add(task); // Add to the end of the list. Always returns true.
        Collections.sort(mTasks, mComparator);
        TaskListDb.removeTaskFromCompleted(task);
    }

    public Task deleteCompletedTask(int at) {
        Task task = mCompletedTasks.remove(at);
        TaskListDb.removeTaskFromCompleted(task);
        return task;
    }

    public void clearCompletedTasks() {
        TaskListDb.clearTasksFromCompleted(mCompletedTasks);
        mCompletedTasks.clear();
    }

    // Add a completed task directly to this TaskList's collection of completed tasks.
    // This differs from completeTask(), which is meant for the swipe to complete gesture, and this
    // method is meant to be a quick way to store an already completed task in this TaskList's collection
    // of completed tasks.
    public void addCompletedTask(Task task) {
        if (!task.isCompleted())
            throw new IllegalStateException("Task must be marked done before calling addCompleteTask()");
        mCompletedTasks.add(task);
        Collections.sort(mCompletedTasks, ComparatorUtils.DATE_COMPLETED);
    }

    public void replaceCompletedTask(int at, Task task) {
        Task prev = mCompletedTasks.set(at, task);
        TaskListDb.replaceTaskInCompleted(prev, task);
    }

    public Task getTask(int i) {
        try {
            return mTasks.get(i);
            // Consider doing this defensive copy instead?
            //return new Task(mTasks.get(i));
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("No Task exists at position " + i);
        }
    }

    public Task getCompletedTask(int at) {
        return mCompletedTasks.get(at);
    }

    public void swapTasks(int from, int to) {
        Collections.swap(mTasks, from, to);
    }

    public void setMenuOptionEnabled(MenuOptions option, boolean enabled) {
        mSupportedMenuOptions.put(option, enabled);
    }

    public boolean isMenuOptionEnabled(MenuOptions option) {
        return mSupportedMenuOptions.get(option);
    }

    public Set<MenuOptions> getDisabledMenuOptions() {
        Set<MenuOptions> set = new LinkedHashSet<>();
        for (Map.Entry<MenuOptions, Boolean> entry : mSupportedMenuOptions.entrySet())
            if (!entry.getValue())
                set.add(entry.getKey());
        return set;
    }

    public void disableAllMenuOptions() {
        for (MenuOptions option : MenuOptions.values()) {
            mSupportedMenuOptions.put(option, false);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TaskList))
            return false;
        TaskList rhs = (TaskList) o;
        return mIndex == rhs.mIndex
                && mColor == rhs.mColor
                && mName.equals(rhs.mName)
                && mComparator.equals(rhs.mComparator)
                && mCompletedTasks.equals(rhs.mCompletedTasks)
                && mTasks.equals(rhs.mTasks)
                && mSupportedMenuOptions.equals(rhs.mSupportedMenuOptions);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + mColor;
        result = 31 * result + mName.hashCode();
        result = 31 * result + mIndex;
        result = 31 * result + mTasks.hashCode();
        result = 31 * result + mCompletedTasks.hashCode();
        result = 31 * result + mComparator.hashCode();
        result = 31 * result + mSupportedMenuOptions.hashCode();
        return result;
    }

    public int indexOf(Task task) {
        return mTasks.indexOf(task);
    }

    private boolean isPrimaryTaskList() {
        return TaskListDb.isAll(this) || TaskListDb.isCompleted(this) || TaskListDb.isImportant(this);
    }
    */
}
