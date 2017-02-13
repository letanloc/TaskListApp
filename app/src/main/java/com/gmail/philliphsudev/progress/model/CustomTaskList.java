package com.gmail.philliphsudev.progress.model;

import android.os.Parcel;
import android.util.Log;

import com.gmail.philliphsudev.progress.TaskListDb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Phillip Hsu on 9/15/2015.
 */
public class CustomTaskList extends TaskList implements TaskList.ModifiableIdentity {

    // Initialize sOffset with the number of PrimaryTaskLists. As CustomTaskLists are made, sOffset
    // will be incremented.
    private static int sOffset = PrimaryTaskList.howMany();

    private String mName;
    private int mColor;
    private int mIndex;
    private List<Task> mCompletedTasks;

    public CustomTaskList() {
        this("", -1);
    }

    public CustomTaskList(String name, int color) {
        super();
        mName = name;
        mColor = color;
        mIndex = sOffset++;
        mCompletedTasks = new ArrayList<>();
    }

    public CustomTaskList(CustomTaskList rhs) {
        super(rhs);
        mName = rhs.mName;
        mColor = rhs.mColor;
        mIndex = rhs.mIndex;
        for (Task task : rhs.mCompletedTasks) {
            mCompletedTasks.add(new Task(task));
        }
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public void setName(String name) {
        mName = name;
    }

    @Override
    public int getColor() {
        return mColor;
    }

    @Override
    public void setColor(int color) {
        mColor = color;
    }

    @Override
    public int getIndex() {
        return mIndex;
    }

    @Override
    public void setIndex(int index) {
        mIndex = index;
        for (Task t : getTasks()) {
            t.setTaskListIndex(mIndex);
        }
    }

    public List<Task> getCompletedTasks() {
        return mCompletedTasks;
    }

    @Override
    public int addTask(Task task) {
        Log.i("CustomTaskList", "addTask()");
        task.setTaskListIndex(mIndex);
        if (task.isCompleted()) {
            addCompletedTask(task);
            return -1; // An index is not actually necessary
        }
        TaskListDb.addTaskToPrimaries(task); // If task is completed, this method handles that as well
        int at = super.addTask(task); // Add the task to this list and return its resulting index.
        task.setMyPosition(size() - 1); // Track the insertion order only in the CustomTaskList, and not in the primaries
        return at;
    }

    @Override
    public int replaceTask(int at, Task task) {
        task.setTaskListIndex(mIndex);
        if (task.isCompleted()) {
            Task t1 = super.deleteTask(at); // Delete just from this list
            TaskListDb.removeTaskFromPrimaries(t1); // Removes from 'All' and 'Important', if applicable
            addCompletedTask(task); // Add to this list's completed and to 'Completed'
            //TaskListDb.addTaskToPrimaries(task);
            return -1;
        } else {
            TaskListDb.replaceTaskInPrimaries(getTask(at), task);
            return super.replaceTask(at, task);
        }
    }

    @Override
    public Task deleteTask(int at) {
        TaskListDb.removeTaskFromPrimaries(getTask(at));
        return super.deleteTask(at);
    }

    @Override
    public boolean deleteTask(Task task) {
        TaskListDb.removeTaskFromPrimaries(task);
        return super.deleteTask(task);
    }

    @Override
    public Task completeTask(int at) {
        TaskListDb.removeTaskFromPrimaries(getTask(at));
        // Remove the task from the list and set it completed
        Task task = super.completeTask(at);
        addCompletedTask(task);
        //TaskListDb.addTaskToPrimaries(task); // Adds to 'Completed' only
        return task;
    }

    @Override
    public boolean completeTask(Task task) {
        TaskListDb.removeTaskFromPrimaries(task);
        // Set completed and remove from list
        boolean completed = super.completeTask(task);
        if (completed) {
            addCompletedTask(task);
            //TaskListDb.addTaskToPrimaries(task); // Adds to 'Completed' only
            return completed;
        }
        return false;
    }

    public void swapTasks(int from, int to) {
        Collections.swap(getTasks(), from, to);
    }

    // Add a completed task directly to this TaskList's collection of completed tasks.
    // This differs from completeTask(), which is meant for the swipe to complete gesture, and this
    // method is meant for scenarios where a task is set completed DURING its creation and is subsequently needed to be added.
    public boolean addCompletedTask(Task task) {
        if (!task.isCompleted())
            throw new IllegalArgumentException("Task must be set completed before calling addCompleteTask()");
        task.setTaskListIndex(mIndex);
        TaskListDb.addTaskToPrimaries(task); // Should just add to 'Completed'
        return mCompletedTasks.add(task);
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
        TaskListDb.removeTaskFromPrimaries(task);
        task.setCompleted(false);
        addTask(task);
    }

    public Task deleteCompletedTask(int at) {
        Task task = mCompletedTasks.remove(at);
        TaskListDb.removeTaskFromPrimaries(task);
        return task;
    }

    public void clearCompletedTasks() {
        TaskListDb.clearCompleted();
        mCompletedTasks.clear();
    }

    public void replaceCompletedTask(int at, Task task) {
        task.setCompleted(true);
        task.setTaskListIndex(mIndex);
        Task t1 = mCompletedTasks.set(at, task);
        TaskListDb.replaceTaskInPrimaries(t1, task);
    }

    public Task getCompletedTask(int at) {
        return mCompletedTasks.get(at);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mName);
        dest.writeInt(mColor);
        dest.writeInt(mIndex);
        dest.writeTypedList(mCompletedTasks);
    }

    public static final Creator<CustomTaskList> CREATOR = new Creator<CustomTaskList>() {
        @Override
        public CustomTaskList createFromParcel(Parcel source) {
            return new CustomTaskList(source);
        }

        @Override
        public CustomTaskList[] newArray(int size) {
            return new CustomTaskList[size];
        }
    };

    private CustomTaskList(Parcel in) {
        super(in);
        mName = in.readString();
        mColor = in.readInt();
        mIndex = in.readInt();
        mCompletedTasks = in.createTypedArrayList(Task.CREATOR);
    }
}
