package com.gmail.philliphsudev.progress.model;

import android.os.Parcel;

import com.gmail.philliphsudev.progress.TaskAdapter;

/**
 * Created by Phillip Hsu on 9/15/2015.
 */
public abstract class PrimaryTaskList extends TaskList {

    private static int sNum; // Indicates how many instances of this class are there

    private final String mName;
    private final int mColor;
    private final int mIndex;
    private TaskAdapter mAdapter; // Since PrimaryTaskLists are tricky with updating tasks, they each have their own adapter instance.

    protected PrimaryTaskList(String name, int color) {
        super();
        mName = name;
        mColor = color;
        mIndex = sNum++; // Assign the current value of sNum to mIndex, and THEN increment.

        // PrimaryTaskLists do not support the following menu options
        setMenuOptionEnabled(MenuOptions.VIEW_COMPLETED_TASKS, false);
        setMenuOptionEnabled(MenuOptions.EDIT_TASK_LIST, false);
        setMenuOptionEnabled(MenuOptions.SORT_BY_MY_ORDER, false);
        // However, they do support sorting by TaskList
        setMenuOptionEnabled(MenuOptions.SORT_BY_TASK_LIST, true);
        // A CompletedTaskList supports no menu options, so that will be handled in its own class
        // TODO ? mAdapter = new TaskAdapter(this);
    }

    protected PrimaryTaskList(PrimaryTaskList rhs) {
        super(rhs);
        mName = rhs.mName;
        mColor = rhs.mColor;
        mIndex = rhs.mIndex;
        //mAdapter = new TaskAdapter(this);
    }

    protected PrimaryTaskList(Parcel in) {
        super(in);
        mName = in.readString();
        mColor = in.readInt();
        mIndex = in.readInt();
        //mAdapter = new TaskAdapter(this);
    }

    public static int howMany() {
        return sNum;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public int getColor() {
        return mColor;
    }

    @Override
    public int getIndex() {
        return mIndex;
    }

    public TaskAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO ? throw new UnsupportedOperationException();
        super.writeToParcel(dest, flags);
        dest.writeString(mName);
        dest.writeInt(mColor);
        dest.writeInt(mIndex);
    }
}
