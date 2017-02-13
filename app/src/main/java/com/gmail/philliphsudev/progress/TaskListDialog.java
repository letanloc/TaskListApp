package com.gmail.philliphsudev.progress;


import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.gmail.philliphsudev.progress.model.PrimaryTaskList;

public class TaskListDialog extends DialogFragment {
    private static final String ARG_TASK_LIST_INDEX = "com.gmail.philliphsudev.progress.ARG_TASK_LIST_INDEX";

    private RecyclerView mRecyclerView;
    private TaskListAdapter mAdapter;  // Experimenting with using the actual adapter type, rather than RecyclerView.Adapter
    private RecyclerView.LayoutManager mLayoutManager;

    private View mAddNewButton;

    private OnTaskListPickedListener mListener;
    private Starter mStarter;

    public interface Starter {
        void startEditTaskList();
    }

    public interface OnTaskListPickedListener {
        void onTaskListPicked(int selected);
    }

    public static TaskListDialog newInstance(int taskListIndex) {
        TaskListDialog dialog = new TaskListDialog();
        Bundle args = new Bundle();
        //args.putParcelable(ARG_TASK_LIST, taskList);
        args.putInt(ARG_TASK_LIST_INDEX, taskListIndex);
        dialog.setArguments(args);
        return dialog;
    }

    public TaskListDialog() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnTaskListPickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnTaskListPickedListener");
        }
        try {
            mStarter = (Starter) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Starter");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getParentFragment() != null) {
            try {
                mListener = (OnTaskListPickedListener) getParentFragment();
            } catch (NullPointerException e) {
                throw new NullPointerException("getParentFragment() returned null in class TaskListDialog");
            } catch (ClassCastException e) {
                throw new ClassCastException(getParentFragment().toString() + " must implement OnTaskListPickedListener");
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_task_list, null);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mAddNewButton = view.findViewById(R.id.add_new);

        mRecyclerView.setHasFixedSize(true);
        mAdapter = new TaskListAdapter(true); // true == excludePrimaryLists
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(),
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        // Can't simply relay the clicked position because, for example, position zero
                        // can either correspond to 'All' or the first custom task list, if the List<TaskList>
                        // that the adapter is working with is excluding primaries
                        int offset = PrimaryTaskList.howMany();
                        mListener.onTaskListPicked(position + offset);
                        Log.i("TaskListDialog", "Picked task list at " + (position + offset));
                        dismiss();
                    }
                }));

        mAddNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskListDialog.this.dismiss();
                mStarter.startEditTaskList();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view).setTitle(R.string.pick_task_list);

        return builder.create();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TaskListDb.unregisterTaskListAdapter(mAdapter);
        mAdapter = null;
    }
}
