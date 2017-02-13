package com.gmail.philliphsudev.progress.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gmail.philliphsudev.progress.CalendarUtils;
import com.gmail.philliphsudev.progress.DueDateTimeDialog;
import com.gmail.philliphsudev.progress.R;
import com.gmail.philliphsudev.progress.TaskListDb;
import com.gmail.philliphsudev.progress.TaskListDialog;
import com.gmail.philliphsudev.progress.model.Task;

public class QuickAddTaskFragment extends Fragment {
    private static final String EXTRA_TASK_LIST_INDEX = "taskList";

    private long mDueOn = 0L; // Needed since this exact time will be set for all following tasks
    private int mListIndex;

    private Toolbar mTopToolbar;
    private Toolbar mBottomToolbar;
    private TextView mListSelection;
    private ImageButton mDoneBtn, mImportantBtn, mSetDateBtn;
    private LinearLayout mDateTimeSelection;
    private TextView mDateText;
    private TextView mTimeText;
    private EditText mEditTaskName;
    private ImageButton mAdd;
    private FloatingActionButton mFab;

    private TaskRelayer mTaskRelayer;

    public interface TaskRelayer {
        // Passes back the task created here to the activity.
        void relayCreatedTask(Task task);
        // Notify the activity to start the EditTaskActivity with the task in-creation.
        void relayExpandTask(Task task);
    }

    public static QuickAddTaskFragment newInstance(int currListIndex) {
        if (currListIndex < 0)
            throw new IndexOutOfBoundsException("Tried to create instance of QuickAddTaskFragment with no last known TaskList position");
        QuickAddTaskFragment fragment = new QuickAddTaskFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_TASK_LIST_INDEX, currListIndex);
        fragment.setArguments(args);
        return fragment;
    }

    public QuickAddTaskFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mTaskRelayer = (TaskRelayer) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement TaskRelayer");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mListIndex = getArguments().getInt(EXTRA_TASK_LIST_INDEX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_quick_add_task, container, false);
        mTopToolbar = (Toolbar) view.findViewById(R.id.top);
        mBottomToolbar = (Toolbar) view.findViewById(R.id.menu);
        mListSelection = (TextView) mTopToolbar.findViewById(R.id.list_selection);
        mAdd = (ImageButton) view.findViewById(R.id.add);
        mEditTaskName = (EditText) view.findViewById(R.id.edit_task_name);
        mDoneBtn = (ImageButton) mBottomToolbar.findViewById(R.id.done);
        mImportantBtn = (ImageButton) mBottomToolbar.findViewById(R.id.important);
        mSetDateBtn = (ImageButton) mBottomToolbar.findViewById(R.id.set_date);
        mDateTimeSelection = (LinearLayout) view.findViewById(R.id.date_time_selection);
        mDateText = (TextView) view.findViewById(R.id.date);
        mTimeText = (TextView) view.findViewById(R.id.time);
        mFab = (FloatingActionButton) view.findViewById(R.id.fab);

        mTopToolbar.inflateMenu(R.menu.quick_add_task_top_toolbar);
        mTopToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
        mTopToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mTopToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        mDoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mImportantBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });
        mSetDateBtn.setOnClickListener(mSetDateClickListener);

        mListSelection.setText(TaskListDb.getList(mListIndex).getName());

        mListSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskListDialog.newInstance(mListIndex).show(getFragmentManager(), "TaskListDialog");
            }
        });

        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditTaskName.length() > 0) {
                    mTaskRelayer.relayCreatedTask(createTask());
                    //Log.i("QuickAddTaskFrag", "TaskListDb.addTask()");
                    //TaskListDb.addTask(createTask());
                }
            }
        });

        /* textMultiline overrides the editor action button, so this became useless
        mEditTaskName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (createTask()) {
                        mTaskRelayer.relayCreatedTask(mTask);
                        return true;
                    }
                    return false;
                }
                return false;
            }
        });*/

        mDateTimeSelection.setOnClickListener(mSetDateClickListener);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEditTaskName.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditTaskName, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_expand:
                mTaskRelayer.relayExpandTask(createTask());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Close the keyboard if it is still active
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive(mEditTaskName)) {
            imm.hideSoftInputFromWindow(mEditTaskName.getWindowToken(), 0);
        }
    }

    public void onDueDateTimeSet(long dueDateTime) {
        mDueOn = dueDateTime;
        if (dueDateTime != 0L) {
            mDateText.setText(CalendarUtils.getDateString(dueDateTime));
            mTimeText.setText(CalendarUtils.getTimeString(dueDateTime));
            mDateTimeSelection.setVisibility(View.VISIBLE);
        } else {
            mDateText.setText("");
            mTimeText.setText("");
            mDateTimeSelection.setVisibility(View.GONE);
        }
    }

    public void onTaskListPicked(int selected) {
        mListIndex = selected;
        mListSelection.setText(TaskListDb.getList(mListIndex).getName());
    }

    private Task createTask() {
        Task task = new Task();
        task.setName(mEditTaskName.getText().toString());
        task.setDueOn(mDueOn);
        task.setTaskListIndex(mListIndex);
        // TODO: Set completed/important by checking if those buttons are checked/highlighted
        mEditTaskName.getText().clear();
        return task;
    }

    private final View.OnClickListener mSetDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DueDateTimeDialog dialog = DueDateTimeDialog.newInstance(mDueOn);
            dialog.show(getFragmentManager(), "DueDateTimeDialog");
        }
    };
}
