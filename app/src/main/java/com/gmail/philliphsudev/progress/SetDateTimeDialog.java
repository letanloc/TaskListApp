package com.gmail.philliphsudev.progress;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.util.Calendar;

/**
 * Created by Phillip Hsu on 8/6/2015.
 */
public class SetDateTimeDialog extends DialogFragment implements DateTimePickerHelper.OnPickerFinishListener {
    private static final int NUM_DATE_TIME_VIEWS = 2;
    private static final String ARG_DATETIME = "datetime";
    private static final String ARG_SET_WHICH = "setWhich";
    private static final String ARG_TARGET_OBJECT = "targetObject";

    private static final Calendar sCalendar = Calendar.getInstance();
    private EditText mDate;
    private EditText mTime;
    private long mDateTime;
    private DateTimePickerHelper mPickerHelper;
    private int mTargetViewId; // The target view that this dialog is made for (either ActionItemFrag's startOn or endOn)

    //private DateTimeSettable mTargetObject;
    //private int mSetWhich;

    private OnDateTimeSetListener mListener;

    public interface OnDateTimeSetListener {
        void onDateTimeSet(int viewId, long time);
    }

    public static SetDateTimeDialog newInstance(int viewId, long datetime) {
        SetDateTimeDialog dialog = new SetDateTimeDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_SET_WHICH, viewId);
        args.putLong(ARG_DATETIME, datetime);
        dialog.setArguments(args);
        return dialog;
    }

    public static SetDateTimeDialog newInstance(Object targetObject, int setWhich) {

        if (!(targetObject instanceof Parcelable)) {
            throw new IllegalArgumentException("targetObject not instanceof Parcelable");
        } else {
            // Check if also instanceof DateTimeSettable
            if (!(targetObject instanceof DateTimeSettable))
                throw new IllegalArgumentException("targetObject instanceof Parcelable, but not DateTimeSettable");
        }

        SetDateTimeDialog dialog = new SetDateTimeDialog();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TARGET_OBJECT, (Parcelable) targetObject);
        args.putInt(ARG_SET_WHICH, setWhich);
        dialog.setArguments(args);
        return dialog;
    }

    public SetDateTimeDialog() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDateTimeSetListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnDateTimeSetListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getParentFragment() != null) {
            try {
                //mListener = (OnDateTimeSavedListener) getParentFragment();
            } catch (ClassCastException e) {
                throw new ClassCastException(getParentFragment().getClass().getSimpleName()
                        + " must implement OnDateTimeSavedListener");
            }
        }

        mPickerHelper = new DateTimePickerHelper(this, NUM_DATE_TIME_VIEWS);
        mTargetViewId = getArguments().getInt(ARG_SET_WHICH);
        mDateTime = getArguments().getLong(ARG_DATETIME);

        /*try {
            mTargetObject = (DateTimeSettable) getArguments().getParcelable(ARG_TARGET_OBJECT);
        } catch (ClassCastException e) {
            throw new ClassCastException("Retrieving targetObject failed to cast to DateTimeSettable");
        }

        mSetWhich = getArguments().getInt(ARG_SET_WHICH);
        switch (mSetWhich) {
            case DateTimeSettable.SET_START:
                mDateTime = mTargetObject.getStartDate();
                break;
            case DateTimeSettable.SET_END:
                mDateTime = mTargetObject.getEndDate();
                break;
            default:
                mDateTime = 0L;
                break;
        }*/

        if (mDateTime != 0L) {
            sCalendar.setTimeInMillis(mDateTime);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_set_date_time, null);
        mDate = (EditText) view.findViewById(R.id.date);
        mTime = (EditText) view.findViewById(R.id.time);

        mDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPickerHelper.showDatePicker(v);
            }
        });
        mTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPickerHelper.showTimePicker(v);
            }
        });

        if (mDateTime != 0L) {
            mPickerHelper.remap(mDate, sCalendar);
            mPickerHelper.remap(mTime, sCalendar);
            updateDate();
            updateTime();
        } else {
            mTime.setEnabled(false);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setView(view).setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*if (mSetWhich == DateTimeSettable.SET_START) {
                    mTargetObject.setStartDate(mDateTime);
                } else if (mSetWhich == DateTimeSettable.SET_END) {
                    mTargetObject.setEndDate(mDateTime);
                }*/
                mListener.onDateTimeSet(mTargetViewId, mDateTime);
            }
        }).create();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        sCalendar.setTimeInMillis(System.currentTimeMillis());
        mDate = null;
        mTime = null;
        mPickerHelper = null;
        //mListener = null;
    }

    @Override
    public void onDatePickerOK(View targetView, int year, int monthOfYear, int dayOfMonth) {
        sCalendar.set(year, monthOfYear, dayOfMonth);
        mDateTime = sCalendar.getTimeInMillis();
        updateDate();
        mTime.setEnabled(true);
        updateTime();
    }

    @Override
    public void onTimePickerOK(View targetView, int hourOfDay, int minute) {
        sCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        sCalendar.set(Calendar.MINUTE, minute);
        mDateTime = sCalendar.getTimeInMillis();
        updateTime();
    }

    private void updateDate() {
        mDate.setText(CalendarUtils.getDateString(mDateTime));
    }

    private void updateTime() {
        mTime.setText(CalendarUtils.getTimeString(mDateTime));
    }
}
