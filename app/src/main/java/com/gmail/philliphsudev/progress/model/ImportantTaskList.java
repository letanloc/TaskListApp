package com.gmail.philliphsudev.progress.model;

import android.os.Parcel;

import java.util.Collection;

/**
 * Created by Phillip Hsu on 9/16/2015.
 */
public class ImportantTaskList extends PrimaryTaskList {

    private static final String NAME = "Important";
    private static final int COLOR = 0;

    public ImportantTaskList() {
        super(NAME, COLOR);
        setMenuOptionEnabled(MenuOptions.SORT_BY_IMPORTANCE, false);
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

    @Override
    public void removeAllTasks(Collection<Task> tasks) {
        for (Task task : tasks)
            if (task.isImportant())
                deleteTask(task); // Will find and delete it, if it exists
    }

    public static final Creator<ImportantTaskList> CREATOR = new Creator<ImportantTaskList>() {
        @Override
        public ImportantTaskList createFromParcel(Parcel source) {
            return new ImportantTaskList(source);
        }

        @Override
        public ImportantTaskList[] newArray(int size) {
            // There can never be more than one instance of ImportantTaskList
            throw new UnsupportedOperationException();
        }
    };

    private ImportantTaskList(Parcel in) {
        super(in);
    }
    
    private void check(Task task) {
        if (!task.isImportant())
            throw new IllegalArgumentException("Task cannot be put in ImportantTaskList");
    }
}
