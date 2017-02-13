package com.gmail.philliphsudev.progress;

import android.support.v4.app.DialogFragment;

/**
 * Created by Phillip Hsu on 7/22/2015.
 */
public class EditListDialog extends DialogFragment {
    /*private static final String ARG_POSITION = "com.gmail.philliphsudev.progress.ARG_POSITION";

    private int mPosition;
    private boolean mIsCreating;
    private OnTaskListAddedListener mListener;

    public interface OnTaskListAddedListener {
        void onTaskListAdded(int position);
        void onTaskListEdited(int position);
    }

    // Takes in the position of the TaskList in editing.
    public static EditListDialog newInstance(int position) {
        EditListDialog dialog = new EditListDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        dialog.setArguments(args);
        return dialog;
    }

    public EditListDialog() {
        // Empty
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mListener = (OnTaskListAddedListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(getParentFragment().toString() + " must implement OnTaskListAddedListener");
        }

        // Dialog was created with a position argument
        if (getArguments() != null) {
            mIsCreating = false;
        } else {
            mIsCreating = true;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_add_list, null);
        final EditText editText = ((EditText)view.findViewById(R.id.edit_list_name));
        if (!mIsCreating) {
            int position = getArguments().getInt(ARG_POSITION);
            // Populate the EditText with the TaskList's name
            editText.setText(TaskListDb.getList(position).getName());
        }
        builder.setView(view)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String title = editText.getText().toString();
                        if (mIsCreating) {
                            int position = TaskListDb.addList(new CustomTaskList(title, -1));
                            mListener.onTaskListAdded(position);
                        } else {
                            int position = getArguments().getInt(ARG_POSITION);
                            TaskListDb.editListName(position, title);
                            mListener.onTaskListEdited(position);
                        }
                        dialog.dismiss();
                    }
                })
                .setTitle(mIsCreating ? R.string.new_task_list : R.string.edit_list);
        final AlertDialog dialog = builder.create();

        // Disable positive button on show
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).
                        setEnabled(editText.getText().length() != 0);
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });

        return dialog;
    }*/
}
