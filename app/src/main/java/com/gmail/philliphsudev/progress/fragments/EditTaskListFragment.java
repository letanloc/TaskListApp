package com.gmail.philliphsudev.progress.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.gmail.philliphsudev.progress.R;
import com.gmail.philliphsudev.progress.TaskListDb;
import com.gmail.philliphsudev.progress.model.CustomTaskList;

public class EditTaskListFragment extends android.support.v4.app.Fragment {
    private static final String ARG_LIST_INDEX = "taskList";

    private CustomTaskList mTaskList; // Can only add/edit CustomTaskList instances
    private int mListIndex;

    private Toolbar mToolbar;
    private EditText mEditListName;
    private Button mDeleteBtn;

    private TaskListRelayer mRelayer;

    public interface TaskListRelayer {
        void relayTaskListAdded(int at);
        void relayTaskListUpdated(int at);
        void relayTaskListDeleted(int at);
    }

    public static EditTaskListFragment newInstance(int listIndex) {
        EditTaskListFragment fragment = new EditTaskListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LIST_INDEX, listIndex);
        fragment.setArguments(args);
        return fragment;
    }

    public EditTaskListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mRelayer = (TaskListRelayer) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement TaskListRelayer");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mListIndex = getArguments().getInt(ARG_LIST_INDEX);
        }
        try {
            Log.i("EditTaskListFrag", "Trying to retrieve TaskList");
            mTaskList = (CustomTaskList) TaskListDb.getList(mListIndex);
        } catch (IndexOutOfBoundsException e) {
            Log.i("EditTaskListFrag", "Failed to retrieve TaskList. Creating new.");
            mTaskList = new CustomTaskList();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_task_list, container, false);
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mEditListName = (EditText) view.findViewById(R.id.edit_list_name);
        mDeleteBtn = (Button) view.findViewById(R.id.delete_btn);

        mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStackImmediate();
                if (!mTaskList.getName().equals(mEditListName.getText().toString())) {
                    mTaskList.setName(mEditListName.getText().toString());
                    if (mTaskList.getIndex() < 0) {
                        mRelayer.relayTaskListAdded(TaskListDb.addList(mTaskList));
                    } else {
                        mRelayer.relayTaskListUpdated(mTaskList.getIndex());
                    }
                }
            }
        });
        mToolbar.setTitle(mTaskList.getIndex() >= 0 ? "Edit task list" : "New task list");
        mEditListName.setText(mTaskList.getName());
        mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Delete " + mTaskList.getName() + "?")
                        .setMessage("Tasks and all their history will be deleted.")
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                TaskListDb.removeList(mTaskList.getIndex());
                                mRelayer.relayTaskListDeleted(mTaskList.getIndex());
                                getFragmentManager().popBackStack();
                            }
                        })
                        .show();
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEditListName.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditListName, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Close the keyboard if it is still active
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive(mEditListName)) {
            imm.hideSoftInputFromWindow(mEditListName.getWindowToken(), 0);
        }
    }

}
