package com.gmail.philliphsudev.progress.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.gmail.philliphsudev.progress.CalendarUtils;
import com.gmail.philliphsudev.progress.DueDateTimeDialog;
import com.gmail.philliphsudev.progress.HistoryAdapter;
import com.gmail.philliphsudev.progress.R;
import com.gmail.philliphsudev.progress.RecyclerItemClickListener;
import com.gmail.philliphsudev.progress.TaskListDb;
import com.gmail.philliphsudev.progress.TaskListDialog;
import com.gmail.philliphsudev.progress.WorkIntervalAdapter;
import com.gmail.philliphsudev.progress.model.History;
import com.gmail.philliphsudev.progress.model.Task;
import com.gmail.philliphsudev.progress.model.WorkInterval;

import java.util.Collections;

public class EditTaskFragment extends android.support.v4.app.Fragment {
    private static final String ARG_TASK = "task";
    private static final String ARG_LAST_POS = "lastPos";

    public static final String EXTRA_TASK = "com.gmail.philliphsudev.progress.fragments.EditTaskFragment.TASK";
    //public static final String EXTRA_DELETE_POSITION = "com.gmail.philliphsudev.progress.fragments.DELETE_POSITION";
    //public static final String EXTRA_CHANGES_MADE = "com.gmail.philliphsudev.progress.fragments.EditTaskFragment.CHANGES_MADE";
    public static final String EXTRA_DELETE = "com.gmail.philliphsudev.progress.fragments.EditTaskFragment.DELETE_TASK";
    public static final String EXTRA_LAST_POS = "com.gmail.philliphsudev.progress.fragments.EditTaskFragment.LAST_POS";

    private Task mTask;
    private boolean mChangesMade;
    private boolean mIsCreating;
    private int mLastPos;
    // Having a "perpetual" instance of each adapter may be nicer? That way, every time we switch
    // we will have the same adapter instead of starting with a new adapter each time.
    private WorkIntervalAdapter mWorkIntervalAdapter;
    private HistoryAdapter mHistoryAdapter;

    private int mSelectedHistoryView = 0;

    private Toolbar mToolbar;
    private EditText mEditTaskName;
    private TextView mDateSelection;
    private TextView mListSelection;
    private Spinner mSpinner;
    private RecyclerView mRecyclerView; // Displays the list of WorkIntervals or History.
    private FloatingActionButton mFab;
    private Button mStartTimingBtn;
    private Menu mMenu;

    private Starter mStarter;

    public interface Starter {
        void startEditWorkInterval(WorkInterval interval);
        void startEditHistoryDetails(History history);
    }

    public static EditTaskFragment newInstance(int lastPos, Task task) {
        if (task == null)
            throw new NullPointerException("Tried to instantiate EditTaskFragment with null Task argument");
        if (task.getTaskListIndex() < 0)
            throw new NullPointerException("Tried to instantiate EditTaskFragment with invalid TaskList index");

        EditTaskFragment fragment = new EditTaskFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TASK, task);
        args.putInt(ARG_LAST_POS, lastPos);
        fragment.setArguments(args);
        return fragment;
    }

    public EditTaskFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mStarter = (Starter) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Starter");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            //mTaskList = getArguments().getParcelable(ARG_TASK_LIST);
            mTask = getArguments().getParcelable(ARG_TASK);
            mLastPos = getArguments().getInt(ARG_LAST_POS);
        }
        if (mLastPos < 0) {
            mIsCreating = true;
            if (!mTask.getName().isEmpty() || mTask.dueOn() != 0L) {
                // These conditions were met from QuickAddTaskFragment
                mChangesMade = true;
            }
        }
        mWorkIntervalAdapter = new WorkIntervalAdapter(mTask.getSchedule());
        mHistoryAdapter = new HistoryAdapter(mTask.getHistories());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_task_v3, container, false);
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mEditTaskName = (EditText) view.findViewById(R.id.edit_task_name);
        mDateSelection = (TextView) view.findViewById(R.id.date_selection);
        mListSelection = (TextView) view.findViewById(R.id.list_selection);
        mSpinner = (Spinner) view.findViewById(R.id.spinner);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mFab = (FloatingActionButton) view.findViewById(R.id.fab);
        mStartTimingBtn = (Button) view.findViewById(R.id.start_timing_btn);

        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        mEditTaskName.setText(mTask.getName());
        mDateSelection.setText(CalendarUtils.getDateTimeString(mTask.dueOn()));
        mListSelection.setText(TaskListDb.getList(mTask.getTaskListIndex()).getName());

        mEditTaskName.requestFocus();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mWorkIntervalAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                int spinnerPos = mSpinner.getSelectedItemPosition();
                if (spinnerPos == 0) {
                    // WorkIntervals are in view
                    if (position == mWorkIntervalAdapter.getItemCount() - 1) {
                        Log.i("EditTaskFrag", "Add work interval button pressed.");
                        mStarter.startEditWorkInterval(null); // Passing null tells the Activity to create a WorkInterval for the Fragment
                    } else {
                        WorkInterval wi = mTask.getWorkInterval(position);
                        wi.setLastPosition(position);
                        mStarter.startEditWorkInterval(wi);
                    }
                } else if (spinnerPos == 1) {
                    // Histories are in view
                    History h = mTask.getHistory(position);
                    h.setLastPosition(position);
                    mStarter.startEditHistoryDetails(h);
                }
            }
        }));

        mEditTaskName.addTextChangedListener(new TextWatcher() {
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
        });

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
                TaskListDialog dialog = TaskListDialog.newInstance(mTask.getTaskListIndex());
                dialog.show(getFragmentManager(), "TaskListDialog");
            }
        });

        mStartTimingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mChangesMade) {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                } else {
                    mTask.setName(mEditTaskName.getText().toString());
                    Intent data = new Intent();
                    data.putExtra(EXTRA_LAST_POS, mLastPos);
                    data.putExtra(EXTRA_TASK, mTask);
                    getActivity().setResult(Activity.RESULT_OK, data);
                }
                NavUtils.navigateUpFromSameTask(getActivity());
            }
        });

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.edit_task_spinner, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    mMenu.findItem(R.id.change_history_view).setVisible(false);
                    /*if (!(mAdapter instanceof WorkIntervalAdapter)) {
                        mRecyclerView.setAdapter(new WorkIntervalAdapter(mTask.getSchedule()));
                    }*/
                    mRecyclerView.setAdapter(mWorkIntervalAdapter);
                } else if (position == 1) {
                    mMenu.findItem(R.id.change_history_view).setVisible(true);
                    mRecyclerView.setAdapter(mHistoryAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_edit_task, menu);
        mMenu = menu;
        menu.findItem(R.id.action_delete).setVisible(!mIsCreating);
        menu.findItem(R.id.change_history_view).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
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
            case R.id.action_done:
                mChangesMade = true;
                item.setChecked(!item.isChecked());
                Log.i("EditTask", "Task completed: " + item.isChecked());
                mTask.setCompleted(item.isChecked());
                return true;
            case R.id.action_important:
                mChangesMade = true;
                item.setChecked(!item.isChecked());
                Log.i("EditTask", "Task important: " + item.isChecked());
                mTask.setImportant(item.isChecked());
                return true;
            case R.id.action_delete:
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
                                // Only relevant extra for deleting a task is the last position of the task
                                Intent data = new Intent();
                                data.putExtra(EXTRA_LAST_POS, mLastPos);
                                data.putExtra(EXTRA_TASK, mTask);
                                data.putExtra(EXTRA_DELETE, true);
                                getActivity().setResult(Activity.RESULT_OK, data);
                                NavUtils.navigateUpFromSameTask(getActivity());
                            }
                        });
                builder.show();
                return true;
            case R.id.change_history_view:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                builder2.setSingleChoiceItems(R.array.history_view_options, mSelectedHistoryView, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSelectedHistoryView = which;
                        switch (which) {
                            case 0:
                                Collections.sort(mTask.getHistories());
                                if (mSpinner.getSelectedItemPosition() == 1) {
                                    mHistoryAdapter.notifyDataSetChanged();
                                }
                                break;
                            case 1:
                                Collections.sort(mTask.getHistories(), Collections.reverseOrder());
                                if (mSpinner.getSelectedItemPosition() == 1) {
                                    mHistoryAdapter.notifyDataSetChanged();
                                }
                                break;
                            case 2:
                                // TODO day
                                break;
                            case 3:
                                // TODO week
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }
                });
                builder2.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditTaskName, InputMethodManager.SHOW_IMPLICIT);
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
        mChangesMade = true;
        mTask.setDueOn(dueDateTime);
        mDateSelection.setText(CalendarUtils.getDateTimeString(dueDateTime));
    }

    public void onTaskListPicked(int selected) {
        mChangesMade = true;
        mListSelection.setText(TaskListDb.getList(selected).getName());
        //mTaskList = selected;
        mTask.setTaskListIndex(selected);
    }

    public void relayWorkInterval(WorkInterval wi) {
        int spinnerPos = mSpinner.getSelectedItemPosition();
        if (wi.getLastPosition() < 0) {
            int pos = mTask.addWorkInterval(wi);
            if (spinnerPos == 0) {
                mWorkIntervalAdapter.notifyItemInserted(pos);
            }
        } else {
            int oldPos = wi.getLastPosition();
            int newPos = mTask.replaceWorkInterval(oldPos, wi);
            if (spinnerPos == 0) {
                if (oldPos != newPos) {
                    mWorkIntervalAdapter.notifyItemMoved(oldPos, newPos);
                }
                mWorkIntervalAdapter.notifyItemChanged(newPos);
            }
        }

        mChangesMade = true;
    }

    public void relayDeleteWorkInterval(WorkInterval wi) {
        if (wi.getLastPosition() >= 0) {
            mTask.deleteWorkInterval(wi.getLastPosition());
            if (mSpinner.getSelectedItemPosition() == 0) {
                mWorkIntervalAdapter.notifyItemRemoved(wi.getLastPosition());
            }
        } else {
            throw new IndexOutOfBoundsException("WorkInterval to be deleted does not have a last known position");
        }

        mChangesMade = true;
    }

    public void relayHistory(History h) {
        mTask.replaceHistory(h.getLastPosition(), h);
        // Don't need to notify adapter of changes, since the "major" History values can't be modified
        // Any changes made will be the descriptions of the events
    }
}
