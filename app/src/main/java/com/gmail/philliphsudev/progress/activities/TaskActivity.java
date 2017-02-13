package com.gmail.philliphsudev.progress.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.gmail.philliphsudev.progress.DueDateTimeDialog;
import com.gmail.philliphsudev.progress.TaskListDb;
import com.gmail.philliphsudev.progress.TaskListDialog;
import com.gmail.philliphsudev.progress.fragments.ActionItemsFragment;
import com.gmail.philliphsudev.progress.fragments.HistoryFragment;
import com.gmail.philliphsudev.progress.fragments.TaskFragment;
import com.gmail.philliphsudev.progress.model.ActionItem;
import com.gmail.philliphsudev.progress.model.History;
import com.gmail.philliphsudev.progress.model.Task;
import com.gmail.philliphsudev.progress.model.TaskList;

import java.util.ArrayList;
import java.util.List;

public class TaskActivity extends AppCompatActivity implements DueDateTimeDialog.OnDateTimeSetListener,
        TaskListDialog.OnTaskListPickedListener, TaskFragment.Starter {

    public static final int REQUEST_SHOW_ACTION_ITEMS = 0;
    public static final int REQUEST_SHOW_HISTORY = 1;

    public static final String EXTRA_ACTION_ITEMS = "com.gmail.philliphsudev.progress.activities.ACTION_ITEMS";
    public static final String EXTRA_TASK_NAME = "com.gmail.philliphsudev.progress.activities.TASK_NAME";
    public static final String EXTRA_HISTORY = "com.gmail.philliphsudev.progress.activities.HISTORY";

    private TaskFragment mTaskFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int taskListIndex = getIntent().getIntExtra(MainActivity.EXTRA_TASK_LIST_INDEX, -1);
        if (taskListIndex == -1)
            throw new IllegalArgumentException("Cannot start TaskActivity with negative taskListIndex");
        TaskList list = TaskListDb.getList(taskListIndex);
        Task task = getIntent().getParcelableExtra(MainActivity.EXTRA_TASK);
        mTaskFragment = TaskFragment.newInstance(list, task);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(
                    android.R.id.content, mTaskFragment).commit();
        }
    }

    @Override
    public void onDueDateTimeSet(long dueDateTime) {
        mTaskFragment.onDueDateTimeSet(dueDateTime);
    }

    @Override
    public void onTaskListPicked(int selected) {
        //mTaskFragment.onTaskListPicked(selected);
    }

    @Override
    public void startActionItemsActivity(String taskName, List<ActionItem> actionItems) {
        Intent intent = new Intent(this, ActionItemsActivity.class);
        intent.putExtra(EXTRA_TASK_NAME, taskName);
        intent.putParcelableArrayListExtra(EXTRA_ACTION_ITEMS, (ArrayList<ActionItem>) actionItems);
        startActivityForResult(intent, REQUEST_SHOW_ACTION_ITEMS);
    }

    @Override
    public void startHistoryActivity(String name, List<History> histories) {
        Intent intent = new Intent(this, HistoryActivity.class);
        intent.putExtra(EXTRA_TASK_NAME, name);
        intent.putParcelableArrayListExtra(EXTRA_HISTORY, (ArrayList<History>) histories);
        startActivityForResult(intent, REQUEST_SHOW_HISTORY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            return;
        }

        switch (requestCode) {
            case REQUEST_SHOW_ACTION_ITEMS:
                boolean actionItemsChanged = data.getBooleanExtra(ActionItemsFragment.EXTRA_HAS_CHANGES, false);
                if (!actionItemsChanged) {
                    Log.i("TaskActivity", "Action items unchanged");
                    return;
                }

                List<ActionItem> items = data.getParcelableArrayListExtra(ActionItemsFragment.EXTRA_FINISHED_ACTION_ITEMS);
                if (items == null) {
                    throw new IllegalArgumentException("Finished list of ActionItems == null");
                }

                mTaskFragment.notifyActionItemsChanged(items);
                break;
            case REQUEST_SHOW_HISTORY:
                boolean historyChanged = data.getBooleanExtra(HistoryFragment.EXTRA_HAS_CHANGES, false);
                if (!historyChanged) {
                    Log.i("TaskActivity", "History unchanged");
                    return;
                }
                ArrayList<History> histories = data.getParcelableArrayListExtra(HistoryFragment.EXTRA_FINISHED_HISTORIES);
                if (histories == null) {
                    throw new NullPointerException("Finished histories == null");
                }
                mTaskFragment.notifyHistoriesChanged(histories);
                break;
            default:
                Log.e("TaskActivity", "onActivityResult failed to match up with any request code");
                break;
        }
    }
}
