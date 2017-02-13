package com.gmail.philliphsudev.progress;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Phillip Hsu on 6/30/2015.
 */
public class DateTimePickerHelper implements
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    public static final String DATE_FORMAT_FULL    = "E, MMM d y";  // e.g. Tue, Jan 15 2015
    //public static final String DATE_FORMAT_SHORT   = "E MMM d";
    public static final String TAG_DATEPICKER      = "DatePickerDialog";
    public static final String TAG_TIMEPICKER      = "TimePickerDialog";

    //private static final boolean IS_24HR_MODE       = false;

    // Each target View is associated with its own retained state calendar
    private Map<View, Calendar> mRetainedCalendarsMap;

    // Tracks the targetViews that are clicked throughout the lifetime of the parent fragment,
    // with the most recent on top. The topmost item is the View that needs to display the date or time.
    private Deque<View> mViewDeque;

    // How many there are is however you defined the XML
    // layout for whichever class is using this helper class
    private int mNumDateTimeViews;

    private android.support.v4.app.Fragment mCallingFragment;

    private OnPickerFinishListener mListener;

    public interface OnPickerFinishListener {
        void onDatePickerOK(View targetView, int year, int monthOfYear, int dayOfMonth);
        void onTimePickerOK(View targetView, int hourOfDay, int minute);
    }

    public DateTimePickerHelper(android.support.v4.app.Fragment fragment, int numDateTimeViews) {
        mCallingFragment = fragment;
        mNumDateTimeViews = numDateTimeViews;
        mRetainedCalendarsMap = new HashMap<>(mNumDateTimeViews);
        mViewDeque = new ArrayDeque<>();
        try {
            mListener = (OnPickerFinishListener) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement OnDialogFinishListener");
        }
    }

    public void showDatePicker(View targetView) {
        Log.d(getClass().getSimpleName(), "Retrieved targetViewId = " + targetView.getId());
        // Track down the target view associated with this picker
        mViewDeque.addFirst(targetView);
        if (!mRetainedCalendarsMap.containsKey(targetView))
            mRetainedCalendarsMap.put(targetView, Calendar.getInstance());
        Calendar cal = mRetainedCalendarsMap.get(targetView);
        if (cal == null) {
            Log.e(getClass().getSimpleName(), "Calendar returned null for targetViewId = " + targetView.getId());
            return;
        }
        DatePickerDialog dpd = DatePickerDialog.newInstance(this,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        dpd.show(mCallingFragment.getActivity().getFragmentManager(), TAG_DATEPICKER);
    }

    public void showTimePicker(View targetView) {
        Log.d(getClass().getSimpleName(), "Retrieved targetViewId = " + targetView.getId());
        // Track down the target view associated with this picker
        mViewDeque.addFirst(targetView);
        if (!mRetainedCalendarsMap.containsKey(targetView)) {
            mRetainedCalendarsMap.put(targetView, Calendar.getInstance());
        }
        Calendar cal = mRetainedCalendarsMap.get(targetView);
        if (cal == null) {
            Log.e(getClass().getSimpleName(), "Calendar returned null for targetViewId = " + targetView.getId());
            return;
        }
        TimePickerDialog tpd = TimePickerDialog.newInstance(this,
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(mCallingFragment.getActivity().getApplicationContext()));
        tpd.show(mCallingFragment.getActivity().getFragmentManager(), TAG_TIMEPICKER);
    }

    public void setDate(View key, int year, int monthOfYear, int dayOfMonth) {
        mRetainedCalendarsMap.get(key).set(year, monthOfYear, dayOfMonth);
    }

    public void setTime(View key, int hourOfDay, int minute) {
        Calendar cal = mRetainedCalendarsMap.get(key);
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
    }

    public void setCalendarToNow(View key) {
        mRetainedCalendarsMap.get(key).setTime(new Date());
    }

    public void setTimeToNow(View key) {
        Calendar cal = mRetainedCalendarsMap.get(key);
        Calendar now = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
    }

    public String getDateString(View key) {
        SimpleDateFormat sdf = new SimpleDateFormat(DateTimePickerHelper.DATE_FORMAT_FULL);
        return sdf.format(mRetainedCalendarsMap.get(key).getTime());
    }

    public String getTimeString(View key) {
        return DateFormat.getTimeFormat(mCallingFragment.getActivity())
                .format(mRetainedCalendarsMap.get(key).getTime());
    }

    public void remap(View key, Calendar calendar) {
        // For when a date and/or time were previously saved for this view
        if (!mRetainedCalendarsMap.containsKey(key))
            mRetainedCalendarsMap.put(key, calendar);
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int monthOfYear, int dayOfMonth) {
        // Peek returns the element at the head (i.e. top) of the deque
        View targetView = mViewDeque.peekFirst();
        // Save date changes to the retained state calendar
        setDate(targetView, year, monthOfYear, dayOfMonth);
        // Let the parent fragment do what it needs with the set date and targetView
        mListener.onDatePickerOK(targetView, year, monthOfYear, dayOfMonth);
        // Empty the deque for the next series of target views
        // Once the date is set, all prior elements in the deque are useless
        mViewDeque.clear();
    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, int minute) {
        View targetView = mViewDeque.peekFirst();
        // Save time changes to the retained state calendar
        setTime(targetView, hourOfDay, minute);
        // Let the parent fragment do what it needs with the set time and targetView
        mListener.onTimePickerOK(targetView, hourOfDay, minute);
        mViewDeque.clear();
    }
}
