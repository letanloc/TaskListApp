package com.gmail.philliphsudev.progress.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gmail.philliphsudev.progress.CalendarUtils;
import com.gmail.philliphsudev.progress.DueDateTimeDialog;
import com.gmail.philliphsudev.progress.R;
import com.gmail.philliphsudev.progress.TaskListDialog;
import com.gmail.philliphsudev.progress.model.ActionItem;
import com.gmail.philliphsudev.progress.model.History;
import com.gmail.philliphsudev.progress.model.Task;
import com.gmail.philliphsudev.progress.model.TaskList;

import java.util.List;

public class TaskFragment extends Fragment {
    private static final String ARG_TASK = "com.gmail.philliphsudev.progress.fragments.TASK";
    private static final String ARG_TASK_LIST = "com.gmail.philliphsudev.progress.fragments.TASK_LIST";

    public static final String EXTRA_FINISHED_TASK = "com.gmail.philliphsudev.progress.fragments.FINISHED_TASK";
    public static final String EXTRA_SELECTED_LIST_INDEX = "com.gmail.philliphsudev.progress.fragments.SELECTED_LIST_INDEX";
    public static final String EXTRA_DELETE_TASK = "com.gmail.philliphsudev.progress.fragments.DELETED_TASK";
    public static final String EXTRA_CHANGES_MADE = "com.gmail.philliphsudev.progress.fragments.CHANGES_MADE";

    private Task mTask;
    private TaskList mTaskList;
    private boolean mIsCreatingTask;
    private boolean mChangesMade;

    private Toolbar mToolbar;
    private EditText mTaskName;
    private EditText mListSelection;
    private EditText mDateSelection;
    private FloatingActionButton mFab;
    private Button mButton;
    private NestedScrollView mScrollView;
    private RelativeLayout mActionItemsLayout;
    private RelativeLayout mHistoryLayout;
    private RelativeLayout mStatsLayout;
    private TextView mActionItemsChanged;
    private TextView mLastActivity; // Most recent History event
    private TextView mHistoryChanged;

    // Use this to disable EditText widgets from displaying context menus (e.g. COPY, PASTE)
    private ActionMode.Callback NO_CONTEXT_MENU = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    };

    private TextWatcher mEditTextChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mChangesMade = true;
        }
    };

    private Starter mListener;

    public interface Starter {
        void startActionItemsActivity(String name, List<ActionItem> actionItems);
        void startHistoryActivity(String name, List<History> histories);
    }

    public static TaskFragment newInstance(TaskList list, Task task) {
        if (list == null || task == null) {
            throw new IllegalArgumentException("Cannot make instance of EditTaskFragment with null argument(s)");
        }
        TaskFragment fragment = new TaskFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TASK_LIST, list);
        args.putParcelable(ARG_TASK, task);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (Starter) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Starter");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mTaskList = getArguments().getParcelable(ARG_TASK_LIST);
            mTask = getArguments().getParcelable(ARG_TASK);
        }
        if (mTask.indexInList() < 0) {
            // Task has never existed prior
            mIsCreatingTask = true;
            // TODO Check if done or important were set
            if (!mTask.getName().isEmpty() || mTask.dueOn() != 0L) {
                mChangesMade = true;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_task_v2, container, false);

        mToolbar             =    (Toolbar) view.findViewById(R.id.toolbar);
        mTaskName            =    (EditText) view.findViewById(R.id.task_name);
        mListSelection       =    (EditText) view.findViewById(R.id.list_selection);
        mDateSelection       =    (EditText) view.findViewById(R.id.date_selection);
        mFab                 =    (FloatingActionButton) view.findViewById(R.id.fab);
        mButton              =    (Button) view.findViewById(R.id.button);
        mScrollView          =    (NestedScrollView) view.findViewById(R.id.scrollview);
        mActionItemsLayout   =    (RelativeLayout) view.findViewById(R.id.action_items_layout);
        mHistoryLayout       =    (RelativeLayout) view.findViewById(R.id.history_layout);
        mStatsLayout         =    (RelativeLayout) view.findViewById(R.id.stats_layout);
        mActionItemsChanged  =    (TextView) view.findViewById(R.id.action_items_changed);
        mHistoryChanged      =    (TextView) view.findViewById(R.id.history_changed);
        mLastActivity        =    (TextView) view.findViewById(R.id.last_activity);

        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        if (((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Disable EditText context menus (e.g. PASTE, COPY, REPLACE)
        mListSelection.setCustomSelectionActionModeCallback(NO_CONTEXT_MENU);
        mDateSelection.setCustomSelectionActionModeCallback(NO_CONTEXT_MENU);

        // Populate fields with provided Task info
        mTaskName.setText(mTask.getName());
        // TODO Enable/disable done and important menu items
        mDateSelection.setText(CalendarUtils.getDateTimeString(mTask.dueOn()));
        mListSelection.setText(mTaskList.getName());

        // Listen to changes made on the EditText widgets
        mTaskName.addTextChangedListener(mEditTextChangeListener);
        mDateSelection.addTextChangedListener(mEditTextChangeListener);
        mListSelection.addTextChangedListener(mEditTextChangeListener);

        mDateSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DueDateTimeDialog dialog = DueDateTimeDialog.newInstance(mTask.dueOn());
                dialog.show(getFragmentManager(), "DueDateTimeDialog");
            }
        });

        mListSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskListDialog dialog = TaskListDialog.newInstance(-1);
                dialog.show(getFragmentManager(), "TaskListDialog");
            }
        });

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTask.setName(mTaskName.getText().toString());
                Intent data = bundleTask(false);
                getActivity().setResult(Activity.RESULT_OK, data);
                NavUtils.navigateUpFromSameTask(getActivity());
            }
        });

        /*mActionItemsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.startActionItemsActivity(mTask.getName(), mTask.getActionItems());
            }
        });

        mHistoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.startHistoryActivity(mTask.getName(), mTask.getHistories());
            }
        });*/

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTaskName.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mTaskName, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Close the keyboard if it is still active
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive(mTaskName)) {
            imm.hideSoftInputFromWindow(mTaskName.getWindowToken(), 0);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_edit_task, menu);
        if (mIsCreatingTask)
            menu.findItem(R.id.action_delete).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            if (mChangesMade) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.discard_changes)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                NavUtils.navigateUpFromSameTask(getActivity());
                            }
                        })
                        .setTitle(R.string.changes_not_saved);
                builder.show();
                return true; // Consume the event here, allowing us to override the behavior
            } else {
                return false; // Allow normal behavior to proceed
            }
        }

        if (id == R.id.action_done) {
            mChangesMade = true;
            mTask.setCompleted(false); // TODO
            return true;
        }

        if (id == R.id.action_important) {
            mChangesMade = true;
            mTask.setImportant(false); // TODO
            return true;
        }

        if (id == R.id.action_delete) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.confirm_delete)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mChangesMade = true;
                            Intent intent = bundleTask(true);
                            getActivity().setResult(Activity.RESULT_OK, intent);
                            NavUtils.navigateUpFromSameTask(getActivity());
                        }
                    });
            builder.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onDueDateTimeSet(long dueDateTime) {
        mChangesMade = true;
        mDateSelection.setText(CalendarUtils.getDateTimeString(dueDateTime));
        mTask.setDueOn(dueDateTime);
    }

    public void onTaskListPicked(TaskList list) {
        mChangesMade = true;
        mListSelection.setText(list.getName());
        mTaskList = list;
    }

    public void notifyActionItemsChanged(List<ActionItem> items) {
        Log.i("TaskFrag", "Notified action items changed");
        mChangesMade = true;
        // TODO Show marker on ActionItemsLayout
        mActionItemsChanged.setVisibility(View.VISIBLE);
       //mTask.setActionItems(items);
    }

    public void notifyHistoriesChanged(List<History> histories) {
        Log.i("TaskFrag", "Notified histories changed");
        mChangesMade = true;
        // TODO Show marker on HistoryLayout
        mHistoryChanged.setVisibility(View.VISIBLE);
        mTask.setHistories(histories); // TODO As of now, the method does nothing
    }

    private Intent bundleTask(boolean delete) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_SELECTED_LIST_INDEX, mTaskList.getIndex());
        intent.putExtra(EXTRA_FINISHED_TASK, mTask);
        intent.putExtra(EXTRA_DELETE_TASK, delete);
        intent.putExtra(EXTRA_CHANGES_MADE, mChangesMade);
        return intent;
    }
}
