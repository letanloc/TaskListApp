package com.gmail.philliphsudev.progress;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.Calendar;

/**
 * Created by Phillip Hsu on 6/30/2015.
 */
public class DueDateTimeDialog extends DialogFragment implements DateTimePickerHelper.OnPickerFinishListener {
    private static final int NUM_DATE_TIME_VIEWS = 2;

    private static final String ARG_DUE_DATETIME = "com.gmail.philliphsudev.progress.ARG_DUE_DATETIME";

    private EditText mEditDate, mEditTime;
    private ImageButton mClearDate, mClearTime;

    private DateTimePickerHelper mPickerHelper = new DateTimePickerHelper(
            DueDateTimeDialog.this, NUM_DATE_TIME_VIEWS);

    private long mDueDateTime;
    private Calendar mDueDateCalendar = Calendar.getInstance();

    private boolean mTimeIsSet = false; // If false, default to 12 AM

    public interface OnDateTimeSetListener {
        void onDueDateTimeSet(long dueDateTime);
    }

    private OnDateTimeSetListener mListener;

    public static DueDateTimeDialog newInstance(long dueDateTime) {
        DueDateTimeDialog dialog = new DueDateTimeDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_DUE_DATETIME, dueDateTime);
        dialog.setArguments(args);
        return dialog;
    }

    public DueDateTimeDialog() {
        // Empty constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDateTimeSetListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnDateTimeSetListener interface");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getParentFragment() != null) {
            try {
                mListener = (OnDateTimeSetListener) getParentFragment();
            } catch (ClassCastException e) {
                throw new ClassCastException("Calling fragment must implement OnDateTimeSetListener interface");
            }
        }

        mDueDateTime = getArguments().getLong(ARG_DUE_DATETIME);
        mDueDateCalendar.setTimeInMillis(mDueDateTime);

        // Oddly, when a task's due date/time is at its default value of 0L (e.g. opening this dialog for the
        // very first time with this task), computing the total milliseconds as done here does not result
        // in zero. This "bug" is due to Calendar internal recalculations because you called get().
        // However, if a user did actually neglect to set a date WITHOUT a time the last time this dialog
        // was opened, then on the next opening of this dialog, the total milliseconds WILL result in zero.
        /*int time = mDueDateCalendar.get(Calendar.MILLISECOND);
        time += mDueDateCalendar.get(Calendar.SECOND) * 1000;
        time += mDueDateCalendar.get(Calendar.MINUTE) * 60 * 1000;
        time += mDueDateCalendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000;
        noTime = time == 0;
        if (noTime) {
            Log.i("Dialog", "Task had no time set: " + noTime);
        } else {
            Log.i("Dialog", "Task time set to: " + time);
        }

        if (mDueDateTime == 0L || noTime) {
            mIsTimeInvalidated = true;
        } else {
            mIsTimeInvalidated = false;
        }

        Log.i("Dialog", "Created with mDueDateTime = " + String.valueOf(mDueDateTime));
        Log.i("Dialog", "Is time invalidated? " + mIsTimeInvalidated);
        */
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_due_date_time, null);
        builder.setView(view)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // Notify TaskFragment to update due date & time detail
                    if (mDueDateTime == 0L) {
                        Log.i("Dialog", "No due date will be set");
                        mListener.onDueDateTimeSet(0L);
                        return;
                    }

                    Log.i("Dialog", "Time is set? " + mTimeIsSet);
                    if (!mTimeIsSet) {
                        // Time was not explicitly set, so default to 12 AM
                        mDueDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
                        mDueDateCalendar.set(Calendar.MINUTE, 0);
                    }

                    mDueDateTime = mDueDateCalendar.getTimeInMillis();
                    /*if (mIsTimeInvalidated) {
                        // Subtract off the stupid time
                        int time = mDueDateCalendar.get(Calendar.MILLISECOND);
                        time += mDueDateCalendar.get(Calendar.SECOND) * 1000;
                        time += mDueDateCalendar.get(Calendar.MINUTE) * 60 * 1000;
                        time += mDueDateCalendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000;
                        Log.i("Dialog", "Subtracting " + time + " ms from mDueDateTime");
                        mDueDateTime -= time;
                    }*/
                    Log.i("Dialog", "Will be setting due datetime to: " + String.valueOf(mDueDateTime));
                    mListener.onDueDateTimeSet(mDueDateTime);
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // User re-opened the dialog immediately after setting a date but without
                    // setting a time. This means the clear time button could be enabled.
                    // Invalidate the time to prevent remapping a nonexistent
                    // time calendar and thus disable the clear time button.
                    //if (!mIsTimeInvalidated) mIsTimeInvalidated = true;
                    //Log.i("Dialog", "Canceling dialog, with mIsTimeInvalidated = " + mIsTimeInvalidated);
                    DueDateTimeDialog.this.getDialog().cancel();
                }
            })
            .setTitle(R.string.set_due_date_time);

        mEditDate = (EditText) view.findViewById(R.id.edit_date);
        mEditTime = (EditText) view.findViewById(R.id.edit_time);
        mClearDate = (ImageButton) view.findViewById(R.id.clear_date);
        mClearTime = (ImageButton) view.findViewById(R.id.clear_time);

        // Task has no due date/time set, OR task has a due date set WITH no time
        if (mDueDateTime != 0L) {
            // Date and possibly time were previously set
            // Remap date
            mPickerHelper.remap(mEditDate, mDueDateCalendar);
            mEditDate.setText(mPickerHelper.getDateString(mEditDate));
            mClearDate.setEnabled(true);
            // Remap time regardless?
            mPickerHelper.remap(mEditTime, mDueDateCalendar);
            mEditTime.setText(mPickerHelper.getTimeString(mEditTime));
            mEditTime.setEnabled(true);
            mClearTime.setEnabled(true);
            mTimeIsSet = true;

            /*if (!mIsTimeInvalidated) {
                Log.i("Dialog", "Time not invalidated, so remapping time ");
                // Time was indeed set, so remap time
                mPickerHelper.remap(mEditTime, mDueDateCalendar);
                mEditTime.setText(mPickerHelper.getTimeString(mEditTime));
                mEditTime.setEnabled(true);
                mClearTime.setEnabled(true);
                mIsTimeInvalidated = false; // just to make sure...
            } else {
                // Date was set WITHOUT a time
                Log.i("Dialog", "Date was previously set WITHOUT time");
                mIsTimeInvalidated = true;
                mEditTime.setEnabled(true);
                mClearTime.setEnabled(false);
            }*/
        } else {
            // By default, disable the editTime field and clear buttons
            mEditTime.setEnabled(false);
            mClearDate.setEnabled(false);
            mClearTime.setEnabled(false);
            mTimeIsSet = false;
        }

        mEditDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPickerHelper.showDatePicker(v);
            }
        });
        mEditTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPickerHelper.showTimePicker(v);
            }
        });
        mClearDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditDate.setText("");
                mEditTime.setText("");
                // Clear the Task's calendar completely
                mDueDateTime = 0L;
                mTimeIsSet = false;
                // Return the retained state calendar associated with mEditDate to current date
                mPickerHelper.setCalendarToNow(mEditDate);
                mEditTime.setEnabled(false);
                mClearTime.setEnabled(false);
                mClearDate.setEnabled(false);
            }
        });
        mClearTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // You can't "clear" a time. It must be restored to the start of day, i.e. 12 AM.
                mEditTime.setText(R.string.hint_default_time);

                // Set the calendar time to 12 AM
                mDueDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
                mDueDateCalendar.set(Calendar.MINUTE, 0);
                mDueDateTime = mDueDateCalendar.getTimeInMillis();
                mTimeIsSet = false;

                // Subtract off the stupid time
                /*mDueDateCalendar.setTimeInMillis(mDueDateTime);
                int time = mDueDateCalendar.get(Calendar.MILLISECOND);
                time += mDueDateCalendar.get(Calendar.SECOND) * 1000;
                time += mDueDateCalendar.get(Calendar.MINUTE) * 60 * 1000;
                time += mDueDateCalendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000;
                Log.i("Dialog", "Subtracting " + time + " ms from mDueDateTime");
                mDueDateTime -= time;*/

                // Restore time fields in retained state calendar associated with mEditTime to current time
                mPickerHelper.setTimeToNow(mEditTime);
                mClearTime.setEnabled(false);
            }
        });

        return builder.create();
    }

    @Override
    public void onDatePickerOK(View targetView, int year, int monthOfYear, int dayOfMonth) {
        Log.i(getClass().getSimpleName(), "Date set to " + (monthOfYear + 1) + "/" + dayOfMonth + "/" + year);

        // Set the calendar date to the user selections
        mDueDateCalendar.set(year, monthOfYear, dayOfMonth);
        // Set the calendar time to the start of that date, i.e. 12 AM
        /*mDueDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        mDueDateCalendar.set(Calendar.MINUTE, 0);*/

        mDueDateTime = mDueDateCalendar.getTimeInMillis();

        if (targetView.getId() == R.id.edit_date) {
            mEditDate.setText(mPickerHelper.getDateString(mEditDate));
        }
        if (!mClearDate.isEnabled())
            mClearDate.setEnabled(true);
        if (!mEditTime.isEnabled())
            mEditTime.setEnabled(true);

        // Set the time to the beginning of the selected date, i.e. 12 AM
        mEditTime.setHint(R.string.hint_default_time);
    }

    @Override
    public void onTimePickerOK(View targetView, int hourOfDay, int minute) {
        Log.i(getClass().getSimpleName(), "Time set to " +hourOfDay+":"+minute);
        mDueDateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mDueDateCalendar.set(Calendar.MINUTE, minute);
        mTimeIsSet = true;

        mDueDateTime = mDueDateCalendar.getTimeInMillis();

        if (targetView.getId() == R.id.edit_time) {
            mEditTime.setText(mPickerHelper.getTimeString(mEditTime));
        }
        if (!mClearTime.isEnabled())
            mClearTime.setEnabled(true);
    }
}
