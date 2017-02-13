package com.gmail.philliphsudev.progress.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.gmail.philliphsudev.progress.ComparatorUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;

/**
 * Created by Phillip Hsu on 7/10/2015.
 */
public abstract class TaskList implements Parcelable {

    public enum MenuOptions { SORT, VIEW_COMPLETED_TASKS, EDIT_TASK_LIST,
        SORT_BY_DUE_DATE,
        SORT_BY_IMPORTANCE,
        SORT_BY_NAME,
        SORT_BY_ORDER_CREATED,
        SORT_BY_TASK_LIST,
        SORT_BY_MY_ORDER
    }

    // Defines methods by which a TaskList can modify values that make up its identity.
    // The values that make up a TaskList's identity are its name, index, and color.
    // Currently, only instances of CustomTaskList can modify their identity.
    public interface ModifiableIdentity {
        void setName(String name);
        void setIndex(int index);
        void setColor(int color);
    }

    public abstract String getName();
    public abstract int getColor();
    public abstract int getIndex();

    // Order and description of indices for any int[] returned by the CRUD methods, meant for
    // updating the TaskAdapter in TaskListFragment of item insertions/removals/etc.
    public static final int INDEX_OF_TASK_IN_ACTUAL = 0;
    public static final int INDEX_OF_TASK_IN_ALL = 1;
    public static final int INDEX_OF_TASK_IN_IMPORTANT = 2;
    public static final int INDEX_OF_TASK_IN_COMPLETED = 3;

    private List<Task> mTasks;
    private Comparator<Task> mComparator = ComparatorUtils.DUE_DATE; // Note: each TaskList instance can have its own sorting mode
    private final EnumMap<MenuOptions, Boolean> mSupportedMenuOptions = new EnumMap<>(MenuOptions.class);

    protected TaskList() {
        mTasks = new ArrayList<>();
        for (MenuOptions option : MenuOptions.values()) {
            // By default, all TaskLists will have all options supported, except for 'Sort by task list'
            // which is only supported by the 'All' primary TaskList
            if (option == MenuOptions.SORT_BY_TASK_LIST)
                mSupportedMenuOptions.put(option, false);
            else
                mSupportedMenuOptions.put(option, true);
        }
    }

    protected TaskList(TaskList rhs) {
        mTasks = new ArrayList<>();
        for (Task task : rhs.mTasks) {
            mTasks.add(new Task(task));
        }
        mComparator = rhs.mComparator;
        for (MenuOptions option : MenuOptions.values()) {
            mSupportedMenuOptions.put(option, rhs.mSupportedMenuOptions.get(option));
        }
    }

    protected TaskList(Parcel in) {
        mTasks = in.createTypedArrayList(Task.CREATOR);
        mComparator = ComparatorUtils.valueOf(in.readInt());
        for (MenuOptions option : MenuOptions.values()) {
            mSupportedMenuOptions.put(option, in.readInt() != 0);
        }
    }

    public List<Task> getTasks() {
        return mTasks;
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

    public int indexOf(Task task) {
        return mTasks.indexOf(task);
    }

    public Task getTask(int at) {
        return mTasks.get(at);
    }

    public boolean containsTask(Task task) {
        return mTasks.contains(task);
    }

    public int addTask(Task task) {
        mTasks.add(task);
        Collections.sort(mTasks, mComparator);
        return mTasks.indexOf(task);
    }

    public int replaceTask(int at, Task task) {
        mTasks.set(at, task);
        Collections.sort(mTasks, mComparator);
        return mTasks.indexOf(task);
    }

    public Task deleteTask(int at) {
        return mTasks.remove(at); // May want to make a Snackbar announcing the deleted Task's name.
    }

    public boolean deleteTask(Task task) {
        return mTasks.remove(task);
    }

    public void removeAllTasks(Collection<Task> tasks) {
        mTasks.removeAll(tasks);
    }

    public Task completeTask(int at) {
        Task task = mTasks.remove(at);
        task.setCompleted(true);
        return task;
        // Don't need to return an index for notifying any adapters of changes because
        // you can never be viewing the completed tasks when you're in the task list.
    }

    public boolean completeTask(Task task) {
        task.setCompleted(true);
        return mTasks.remove(task);
    }

    public void setMenuOptionEnabled(MenuOptions option, boolean enabled) {
        mSupportedMenuOptions.put(option, enabled);
    }

    public boolean isMenuOptionEnabled(MenuOptions option) {
        return mSupportedMenuOptions.get(option);
    }

    public void disableAllMenuOptions() {
        for (MenuOptions option : MenuOptions.values()) {
            mSupportedMenuOptions.put(option, false);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mTasks);
        dest.writeInt(ComparatorUtils.intKey(mComparator));
        for (boolean b : mSupportedMenuOptions.values()) {
            dest.writeInt(b ? 1 : 0);
        }
    }
}
