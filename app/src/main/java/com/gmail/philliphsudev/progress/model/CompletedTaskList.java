package com.gmail.philliphsudev.progress.model;

import android.os.Parcel;

/**
 * Created by Phillip Hsu on 9/16/2015.
 */
public class CompletedTaskList extends PrimaryTaskList {

    private static final String NAME = "Completed";
    private static final int COLOR = 0;

    public CompletedTaskList() {
        super(NAME, COLOR);
        disableAllMenuOptions();
    }

    @Override
    public int addTask(Task task) {
        check(task);
        return super.addTask(task);
    }

    @Override
    public int replaceTask(int at, Task task) {
        check(task);
        return super.replaceTask(at, task);
    }

    public static final Creator<CompletedTaskList> CREATOR = new Creator<CompletedTaskList>() {
        @Override
        public CompletedTaskList createFromParcel(Parcel source) {
            return new CompletedTaskList(source);
        }

        @Override
        public CompletedTaskList[] newArray(int size) {
            // There can never be more than one instance of CompletedTaskList
            throw new UnsupportedOperationException();
        }
    };

    private CompletedTaskList(Parcel in) {
        super(in);
    }

    private void check(Task task) {
        if (!task.isCompleted())
            throw new IllegalArgumentException("Task cannot be put in CompletedTaskList");
    }
}
