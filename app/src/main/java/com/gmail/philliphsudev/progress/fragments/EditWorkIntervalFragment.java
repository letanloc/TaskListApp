package com.gmail.philliphsudev.progress.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.philliphsudev.progress.CalendarUtils;
import com.gmail.philliphsudev.progress.R;
import com.gmail.philliphsudev.progress.SetDateTimeDialog;
import com.gmail.philliphsudev.progress.model.WorkInterval;

public class EditWorkIntervalFragment extends android.support.v4.app.Fragment {
    private static final String ARG_WORK_INTERVAL = "workInterval";
    
    private WorkInterval mWorkInterval;
    
    private Toolbar mToolbar;
    private EditText mEditDesc;
    private Button mIncMin, mDecMin, mIncHour, mDecHour;
    private TextView mHours, mMinutes, mStartOn, mEndOn;
    private TextView mRepeat;
    private MenuItem mSave;
    private MenuItem mDelete;

    private Relayer mRelayer;

    public interface Relayer {
        void relayWorkInterval(WorkInterval wi);
        void relayDeleteWorkInterval(WorkInterval wi);
    }
    
    // TODO: Rename and change types and number of parameters
    public static EditWorkIntervalFragment newInstance(WorkInterval wi) {
        if (wi == null)
            throw new NullPointerException("Tried to make instance of EditWorkIntervalFragment with null WorkInterval arg");
        EditWorkIntervalFragment fragment = new EditWorkIntervalFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_WORK_INTERVAL, wi);
        fragment.setArguments(args);
        return fragment;
    }

    public EditWorkIntervalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mRelayer = (Relayer) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Relayer");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mWorkInterval = getArguments().getParcelable(ARG_WORK_INTERVAL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_work_interval, container, false);
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mEditDesc = (EditText) view.findViewById(R.id.description);
        mHours = (TextView) view.findViewById(R.id.hr);
        mMinutes = (TextView) view.findViewById(R.id.min);
        mStartOn = (TextView) view.findViewById(R.id.start_on);
        mEndOn = (TextView) view.findViewById(R.id.end_on);
        mIncMin = (Button) view.findViewById(R.id.increment_min);
        mDecMin = (Button) view.findViewById(R.id.decrement_min);
        mIncHour = (Button) view.findViewById(R.id.increment_hr);
        mDecHour = (Button) view.findViewById(R.id.decrement_hr);
        mRepeat = (TextView) view.findViewById(R.id.repeat);

        mToolbar.inflateMenu(R.menu.menu_edit_work_interval);
        mSave = mToolbar.getMenu().findItem(R.id.action_save);
        mDelete = mToolbar.getMenu().findItem(R.id.action_delete);
        //mSave.setEnabled(mWorkInterval.getStartDate() < mWorkInterval.getEndDate());
        mDelete.setVisible(mWorkInterval.getLastPosition() >= 0);
        mToolbar.setTitle(mWorkInterval.getLastPosition() < 0 ? "New work interval" : "Edit work interval");
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
        mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        mEditDesc.setText(mWorkInterval.getDescription());

        mEndOn.setEnabled(mWorkInterval.getEndDate() != 0L);
        updateHour(); updateMinute(); updateStartsOn(); updateEndsOn();

        mIncHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWorkInterval.incrementHour();
                updateHour();
                updateEndsOn();
                mSave.setEnabled(mWorkInterval.getStartDate() < mWorkInterval.getEndDate());

            }
        });
        mDecHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWorkInterval.decrementHour();
                updateHour();
                updateEndsOn();
                mSave.setEnabled(mWorkInterval.getStartDate() < mWorkInterval.getEndDate());

            }
        });
        mIncMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWorkInterval.incrementMinutes();
                updateMinute();
                updateEndsOn();
                mSave.setEnabled(mWorkInterval.getStartDate() < mWorkInterval.getEndDate());

            }
        });
        mDecMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWorkInterval.decrementMinutes();
                updateMinute();
                updateEndsOn();
                mSave.setEnabled(mWorkInterval.getStartDate() < mWorkInterval.getEndDate());

            }
        });
        mStartOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetDateTimeDialog dialog = SetDateTimeDialog.newInstance(mStartOn.getId(), mWorkInterval.getStartDate());
                //SetDateTimeDialog dialog = SetDateTimeDialog.newInstance(mWorkInterval, DateTimeSettable.SET_START);
                dialog.show(getFragmentManager(), "start");
            }
        });
        mEndOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetDateTimeDialog dialog = SetDateTimeDialog.newInstance(mEndOn.getId(), mWorkInterval.getEndDate());
                //SetDateTimeDialog dialog = SetDateTimeDialog.newInstance(mWorkInterval, DateTimeSettable.SET_END);
                dialog.show(getFragmentManager(), "end");
            }
        });

        return view;
    }

    /*@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_edit_work_interval, menu);
        mSave = menu.findItem(R.id.action_save);
        mSave.setEnabled(mWorkInterval.getStartDate() < mWorkInterval.getEndDate());
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == mSave) {
            if (mWorkInterval.getStartDate() < mWorkInterval.getEndDate()) {
                mWorkInterval.setDescription(mEditDesc.getText().toString());
                mRelayer.relayWorkInterval(mWorkInterval);
            } else {
                Toast.makeText(getActivity(), "Work interval not created.", Toast.LENGTH_SHORT).show();
            }
            getFragmentManager().popBackStack();
            return true;
        }

        if (item.getItemId() == R.id.action_delete) {
            mRelayer.relayDeleteWorkInterval(mWorkInterval);
            getFragmentManager().popBackStack();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onDateTimeSet(int viewId, long time) {
        switch (viewId) {
            case R.id.start_on:
                mWorkInterval.setStartDate(time);
                updateStartsOn();
                if (!mEndOn.isEnabled()) {
                    mEndOn.setEnabled(mStartOn.getText().length() != 0);
                }
                updateEndsOn();
                break;
            case R.id.end_on:
                mWorkInterval.setEndDate(time);
                updateEndsOn();
                updateDuration();
                break;
            default:
                Log.e("onDateTimeSet()", "Called with a View that does not exist");
                break;
        }
        //mSave.setEnabled(mWorkInterval.getStartDate() < mWorkInterval.getEndDate());
    }

    private void updateStartsOn() {
        mStartOn.setText(CalendarUtils.getDateTimeString(mWorkInterval.getStartDate()));
    }

    private void updateEndsOn() {
        if (mEndOn.isEnabled() && mStartOn.length() > 0) {
            mEndOn.setText(CalendarUtils.getDateTimeString(mWorkInterval.getEndDate()));
        }
    }

    private void updateHour() {
        mHours.setText(String.valueOf(mWorkInterval.getHours()));
    }

    private void updateMinute() {
        mMinutes.setText(String.valueOf(mWorkInterval.getMinutes()));
    }

    private void updateDuration() {
        updateHour();
        updateMinute();
    }

}
