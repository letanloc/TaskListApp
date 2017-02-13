package com.gmail.philliphsudev.progress.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.gmail.philliphsudev.progress.DueDateTimeDialog;
import com.gmail.philliphsudev.progress.R;
import com.gmail.philliphsudev.progress.TaskListDialog;
import com.gmail.philliphsudev.progress.fragments.EditTaskFragment;
import com.gmail.philliphsudev.progress.fragments.EditTaskListFragment;
import com.gmail.philliphsudev.progress.fragments.QuickAddTaskFragment;
import com.gmail.philliphsudev.progress.fragments.TaskListFragment;
import com.gmail.philliphsudev.progress.model.PrimaryTaskList;
import com.gmail.philliphsudev.progress.model.Task;

public class FirstActivity extends AppCompatActivity implements TaskListFragment.NewStarter,
        QuickAddTaskFragment.TaskRelayer, DueDateTimeDialog.OnDateTimeSetListener, TaskListDialog.OnTaskListPickedListener,
        EditTaskListFragment.TaskListRelayer, TaskListDialog.Starter {
    public static final int REQUEST_EDIT_TASK = 1;
    public static final int REQUEST_CREATE_TASK = 2;

    public static final String EXTRA_TASK = "com.gmail.philliphsudev.progress.activities.FirstActivity.TASK";
    public static final String EXTRA_LAST_POS = "com.gmail.philliphsudev.progress.activities.FirstActivity.LAST_POS";

    private TaskListFragment mTaskListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        mTaskListFragment = (TaskListFragment) getSupportFragmentManager().findFragmentById(R.id.task_list_fragment);
    }

    @Override
    public void startQuickAddTask(int listIndex) {
        QuickAddTaskFragment frag = QuickAddTaskFragment.newInstance(listIndex);
        getSupportFragmentManager().beginTransaction().hide(mTaskListFragment)
                .add(android.R.id.content, frag, "QuickAddTaskFragment").addToBackStack(null).commit();
    }

    @Override
    public void startEditTask(int lastPos, Task task) {
        Intent i = new Intent(this, EditTaskActivity.class);
        i.putExtra(EXTRA_LAST_POS, lastPos);
        i.putExtra(EXTRA_TASK, task);
        startActivityForResult(i, REQUEST_EDIT_TASK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED)
            return;

        // First, check if the returned result is an intention to delete the Task
        /*int deleteAt = data.getIntExtra(EditTaskFragment.EXTRA_DELETE_POSITION, -1);
        if (deleteAt >= 0) {
            mTaskListFragment.notifyTaskDeleted(deleteAt);
            return;
        }

        // Check if the returned data actually has any changes
        boolean taskChanged = data.getBooleanExtra(EditTaskFragment.EXTRA_CHANGES_MADE, false);
        if (!taskChanged) {
            return;
        }*/

        Task task = data.getParcelableExtra(EditTaskFragment.EXTRA_TASK);
        if (task == null) {
            throw new NullPointerException("Finished task == null");
        }
        if (task.getTaskListIndex() < PrimaryTaskList.howMany()) {
            throw new IllegalStateException("Finished task has invalid TaskList index");
        }
        int lastPos = data.getIntExtra(EditTaskFragment.EXTRA_LAST_POS, -1);

        boolean delete = data.getBooleanExtra(EditTaskFragment.EXTRA_DELETE, false);
        if (delete) {
            mTaskListFragment.deleteTask(lastPos, task);
            return;
        }

        if (requestCode == REQUEST_CREATE_TASK) {
            //mTaskListFragment.notifyTaskCreated(task);
            mTaskListFragment.addTask(task);
        } else if (requestCode == REQUEST_EDIT_TASK) {
            //mTaskListFragment.notifyTaskUpdated(task);
            if (lastPos < 0) {
                throw new IllegalArgumentException("Cannot update task at invalid position");
            }
            mTaskListFragment.updateTask(lastPos, task);
        }
    }

    // Relay a task created entirely within QuickAddTaskFragment. (i.e. did not expand to finish)
    @Override
    public void relayCreatedTask(Task task) {
        //mTaskListFragment.notifyTaskCreated(task);
        mTaskListFragment.addTask(task);
    }

    // Expand a task started in QuickAddTaskFragment to complete in the full window.
    @Override
    public void relayExpandTask(Task task) {
        getSupportFragmentManager().popBackStackImmediate(); // Pop off the QuickAddTaskFrag
        Intent intent = new Intent(this, EditTaskActivity.class);
        intent.putExtra(EXTRA_TASK, task);
        startActivityForResult(intent, REQUEST_CREATE_TASK);
        //startActivity(intent);
    }

    @Override
    public void onDueDateTimeSet(long dueDateTime) {
        QuickAddTaskFragment frag = (QuickAddTaskFragment) getSupportFragmentManager().findFragmentByTag("QuickAddTaskFragment");
        frag.onDueDateTimeSet(dueDateTime);
    }

    @Override
    public void onTaskListPicked(int selected) {
        QuickAddTaskFragment frag = (QuickAddTaskFragment) getSupportFragmentManager().findFragmentByTag("QuickAddTaskFragment");
        frag.onTaskListPicked(selected);
    }

    @Override
    public void relayTaskListAdded(int at) {
        // This method takes one of two possible actions, depending on the topmost
        // fragment at the time we reach this method.
        // If the topmost fragment is QuickAddTaskFragment, this method should
        // relay the recently added TaskList as the selection for the target TaskList.
        // Otherwise, the topmost fragment is TaskListFragment and this method should
        // hand over responsibility to the fragment to react to the added TaskList.
        Log.i("FirstActivity", "relayTaskListAdded()");
        Fragment topmost = getSupportFragmentManager().findFragmentById(android.R.id.content);
        if (topmost instanceof QuickAddTaskFragment) {
            Log.i("FirstActivity", "Topmost fragment == QuickAddTaskFrag");
            ((QuickAddTaskFragment)topmost).onTaskListPicked(at);
        } else {
            Log.i("FirstActivity", "Topmost fragment == TaskListFrag");
            mTaskListFragment.relayTaskListAdded(at);
        }
    }

    @Override
    public void relayTaskListUpdated(int at) {
        mTaskListFragment.relayTaskListUpdated(at);
    }

    @Override
    public void relayTaskListDeleted(int at) {
        mTaskListFragment.relayTaskListDeleted(at);
    }

    // This is implemented from the Starter interface defined in TaskListFragment.
    @Override
    public void startEditTaskList(int listIndex) {
        EditTaskListFragment frag = EditTaskListFragment.newInstance(listIndex);
        getSupportFragmentManager().beginTransaction().hide(mTaskListFragment)
                .add(android.R.id.content, frag, "EditTaskListFragment")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).addToBackStack(null).commit();
    }

    // This is implemented from the Starter interface defined in TaskListDialog.
    @Override
    public void startEditTaskList() {
        // As of now, the QuickAddTaskFragment should be the topmost fragment.
        // When we return from EditTaskListFragment, we should return to QuickAddTaskFragment.
        Fragment topmost = getSupportFragmentManager().findFragmentById(android.R.id.content);
        EditTaskListFragment frag = EditTaskListFragment.newInstance(-1);
        getSupportFragmentManager().beginTransaction().hide(topmost)
                .add(android.R.id.content, frag, "EditTaskListFragment")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).addToBackStack(null).commit();
    }
}
