package com.gmail.philliphsudev.progress.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.gmail.philliphsudev.progress.DueDateTimeDialog;
import com.gmail.philliphsudev.progress.fragments.QuickAddTaskFragment;
import com.gmail.philliphsudev.progress.fragments.TaskFragment;
import com.gmail.philliphsudev.progress.fragments.TaskListFragment;
import com.gmail.philliphsudev.progress.model.Task;

public class MainActivity extends AppCompatActivity implements TaskListFragment.Starter,
        QuickAddTaskFragment.TaskRelayer, DueDateTimeDialog.OnDateTimeSetListener {
    public static final String TAG_QUICK_ADD_TASK_FRAGMENT = "QuickAddTaskFragment";

    public static final int REQUEST_FINISH_TASK_IN_FULL_WINDOW = 0;
    public static final int REQUEST_SHOW_TASK = 1;

    public static final String EXTRA_TASK = "com.gmail.philliphsudev.progress.activities.TASK";
    public static final String EXTRA_TASK_LIST_INDEX = "com.gmail.philliphsudev.progress.activities.TASK_LIST_INDEX";

    private TaskListFragment mTaskListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTaskListFragment = new TaskListFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, mTaskListFragment).commit();
        }
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment quickAdd = getSupportFragmentManager().findFragmentByTag(TAG_QUICK_ADD_TASK_FRAGMENT);
                mTaskListFragment.reactToQuickAdd(quickAdd != null);
            }
        });
    }

    // Relay a task created entirely within QuickAddTaskFragment. (i.e. did not expand to finish)
    //@Override
    public void relayCreatedTask(Task task) {
        mTaskListFragment.notifyTaskCreated(task);
    }

    // Expand a task started in QuickAddTaskFragment to complete in the full window.
    @Override
    public void relayExpandTask(Task task) {
        getSupportFragmentManager().popBackStackImmediate(); // Pop off the QuickAddTaskFrag
        Intent intent = new Intent(this, TaskActivity.class);
        intent.putExtra(EXTRA_TASK_LIST_INDEX, mTaskListFragment.indexOfCurrentTaskList());
        intent.putExtra(EXTRA_TASK, task);
        startActivityForResult(intent, REQUEST_FINISH_TASK_IN_FULL_WINDOW);
    }

    @Override
    public void startQuickAddTask() {
        getSupportFragmentManager().beginTransaction().add(
                android.R.id.content,
                new QuickAddTaskFragment(),
                TAG_QUICK_ADD_TASK_FRAGMENT
        ).addToBackStack(null).commit();
    }

    @Override
    public void startTaskActivity(int taskListIndex, Task task) {
        Intent intent = new Intent(this, TaskActivity.class);
        intent.putExtra(EXTRA_TASK_LIST_INDEX, taskListIndex);
        intent.putExtra(EXTRA_TASK, task);
        startActivityForResult(intent, REQUEST_SHOW_TASK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED)
            return;

        boolean taskChanged = data.getBooleanExtra(TaskFragment.EXTRA_CHANGES_MADE, false);
        if (!taskChanged) {
            return;
        }

        int taskListIndex = data.getIntExtra(TaskFragment.EXTRA_SELECTED_LIST_INDEX, -1);
        if (taskListIndex == -1) {
            throw new IllegalArgumentException("Selected task list index == -1");
        }

        Task task = data.getParcelableExtra(TaskFragment.EXTRA_FINISHED_TASK);
        if (task == null) {
            throw new IllegalArgumentException("Finished task == null");
        }

        if (requestCode == REQUEST_FINISH_TASK_IN_FULL_WINDOW) {
            mTaskListFragment.notifyTaskCreated(taskListIndex, task);
        } else if (requestCode == REQUEST_SHOW_TASK) {
            boolean delete = data.getBooleanExtra(TaskFragment.EXTRA_DELETE_TASK, false);
            if (delete) {
                mTaskListFragment.notifyTaskDeleted(task);
            } else {
                mTaskListFragment.notifyTaskUpdated(task);
            }
        }
    }

    @Override
    public void onDueDateTimeSet(long dueDateTime) {
        ((QuickAddTaskFragment)getSupportFragmentManager().
                findFragmentByTag(TAG_QUICK_ADD_TASK_FRAGMENT)).onDueDateTimeSet(dueDateTime);
    }
}
