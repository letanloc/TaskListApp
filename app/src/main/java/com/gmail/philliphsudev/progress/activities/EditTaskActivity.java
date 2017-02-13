package com.gmail.philliphsudev.progress.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.gmail.philliphsudev.progress.DueDateTimeDialog;
import com.gmail.philliphsudev.progress.SetDateTimeDialog;
import com.gmail.philliphsudev.progress.TaskListDialog;
import com.gmail.philliphsudev.progress.fragments.DetailedHistoryFragment;
import com.gmail.philliphsudev.progress.fragments.EditTaskFragment;
import com.gmail.philliphsudev.progress.fragments.EditTaskListFragment;
import com.gmail.philliphsudev.progress.fragments.EditWorkIntervalFragment;
import com.gmail.philliphsudev.progress.model.History;
import com.gmail.philliphsudev.progress.model.Task;
import com.gmail.philliphsudev.progress.model.WorkInterval;

public class EditTaskActivity extends AppCompatActivity implements DueDateTimeDialog.OnDateTimeSetListener,
        TaskListDialog.OnTaskListPickedListener, SetDateTimeDialog.OnDateTimeSetListener, EditTaskFragment.Starter,
        EditWorkIntervalFragment.Relayer, DetailedHistoryFragment.HistoryRelayer, EditTaskListFragment.TaskListRelayer,
        TaskListDialog.Starter {

    private EditTaskFragment mEditTaskFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Task task = getIntent().getParcelableExtra(FirstActivity.EXTRA_TASK);
        if (task.getTaskListIndex() < 0) {
            throw new IllegalStateException("Started EditTaskActivity with a Task that has an invalid TaskList index");
        }
        int lastPos = getIntent().getIntExtra(FirstActivity.EXTRA_LAST_POS, -1);
        mEditTaskFragment = EditTaskFragment.newInstance(lastPos, task);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, mEditTaskFragment).commit();
        }
    }

    @Override
    public void onDueDateTimeSet(long dueDateTime) {
        mEditTaskFragment.onDueDateTimeSet(dueDateTime);
    }

    @Override
    public void onTaskListPicked(int selected) {
        mEditTaskFragment.onTaskListPicked(selected);
    }

    @Override
    public void onDateTimeSet(int viewId, long time) {
        EditWorkIntervalFragment frag = (EditWorkIntervalFragment)
                getSupportFragmentManager().findFragmentByTag("EditWorkIntervalFragment");
        frag.onDateTimeSet(viewId, time);
    }

    @Override
    public void startEditWorkInterval(WorkInterval interval) {
        WorkInterval wi;
        if (interval == null) {
            wi = new WorkInterval();
        } else {
            wi = new WorkInterval(interval);
        }

        EditWorkIntervalFragment frag = EditWorkIntervalFragment.newInstance(wi);
        getSupportFragmentManager().beginTransaction().hide(mEditTaskFragment).add(
                android.R.id.content, frag, "EditWorkIntervalFragment")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).addToBackStack(null).commit();
    }

    @Override
    public void startEditHistoryDetails(History history) {
        History copy = new History(history);
        DetailedHistoryFragment frag = DetailedHistoryFragment.newInstance(copy);
        getSupportFragmentManager().beginTransaction().hide(mEditTaskFragment).add(
                android.R.id.content, frag, "HistoryDetailsFragment")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).addToBackStack(null).commit();
    }

    @Override
    public void relayWorkInterval(WorkInterval wi) {
        mEditTaskFragment.relayWorkInterval(wi);
    }

    @Override
    public void relayDeleteWorkInterval(WorkInterval wi) {
        mEditTaskFragment.relayDeleteWorkInterval(wi);
    }

    @Override
    public void relayHistory(History h) {
        mEditTaskFragment.relayHistory(h);
    }

    @Override
    public void startEditTaskList() {
        EditTaskListFragment frag = EditTaskListFragment.newInstance(-1);
        getSupportFragmentManager().beginTransaction().hide(mEditTaskFragment)
                .add(android.R.id.content, frag, "EditTaskListFragment")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).addToBackStack(null).commit();
    }

    @Override
    public void relayTaskListAdded(int at) {
        // The TaskListDialog was dismissed upon starting EditTaskListFragment, so no need to update adapter.
        // Instead, need to set EditTaskFragment's task list selection to this one.
        mEditTaskFragment.onTaskListPicked(at);
    }

    @Override
    public void relayTaskListUpdated(int at) {
        // Can't update TaskLists in this Activity.
    }

    @Override
    public void relayTaskListDeleted(int at) {
        // Can't delete TaskLists in this Activity.
    }
}
