package com.gmail.philliphsudev.progress;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gmail.philliphsudev.progress.model.WorkInterval;

/**
 * Created by Phillip Hsu on 8/17/2015.
 */
public class WorkIntervalView extends LinearLayout implements WorkInterval.ViewUpdater {
    
    private Button mIncMin, mDecMin, mIncHour, mDecHour;
    private TextView mHours, mMinutes;
    private EditText mStartOn, mEndOn;
    private ImageButton mDelete;
    private ImageButton mSave;

    private WorkInterval mWorkInterval;

    private WorkIntervalViewActionListener mListener;

    public void setWorkIntervalViewActionListener(WorkIntervalViewActionListener listener) {
        mListener = listener;
    }

    public interface WorkIntervalViewActionListener {
        void onWorkIntervalViewSave(WorkInterval interval);
        void onWorkIntervalViewDelete(WorkInterval interval);
    }

    public WorkIntervalView(final Context context) {
        super(context);
        
        setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        
        // Get the number of pixels corresponding to 16dp
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        layoutParams.setMarginStart(px);
        layoutParams.setMarginEnd(px);
        setLayoutParams(layoutParams);

        LayoutInflater.from(context).inflate(R.layout.work_interval, this, true); // this = root, true for attachToRoot
        mHours = (TextView) findViewById(R.id.hr);
        mMinutes = (TextView) findViewById(R.id.min);
        mStartOn = (EditText) findViewById(R.id.start_on);
        mEndOn = (EditText) findViewById(R.id.end_on);
        mIncMin = (Button) findViewById(R.id.increment_min);
        mDecMin = (Button) findViewById(R.id.decrement_min);
        mIncHour = (Button) findViewById(R.id.increment_hr);
        mDecHour = (Button) findViewById(R.id.decrement_hr);
        mDelete = (ImageButton) findViewById(R.id.delete);
        mSave = (ImageButton) findViewById(R.id.save);

        // By default
        mSave.setEnabled(false);

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
                //SetDateTimeDialog dialog = SetDateTimeDialog.newInstance(mStartOn.getId(), mWorkInterval.getStartDate());
                SetDateTimeDialog dialog = SetDateTimeDialog.newInstance(mWorkInterval, DateTimeSettable.SET_START);
                try {
                    dialog.show(((AppCompatActivity)context).getSupportFragmentManager(), "start");
                } catch (ClassCastException e) {
                    throw new ClassCastException("Context is not an instance of AppCompatActivity");
                }
            }
        });
        mEndOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //SetDateTimeDialog dialog = SetDateTimeDialog.newInstance(mEndOn.getId(), mWorkInterval.getEndDate());
                SetDateTimeDialog dialog = SetDateTimeDialog.newInstance(mWorkInterval, DateTimeSettable.SET_END);
                try {
                    dialog.show(((AppCompatActivity)context).getSupportFragmentManager(), "end");
                } catch (ClassCastException e) {
                    throw new ClassCastException("Context is not an instance of AppCompatActivity");
                }
            }
        });
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener == null) {
                    throw new NullPointerException("Did not implement OnWorkIntervalChangeListener");
                }
                mListener.onWorkIntervalViewDelete(mWorkInterval);
            }
        });
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener == null) {
                    throw new NullPointerException("Did not implement OnWorkIntervalChangeListener");
                }
                mListener.onWorkIntervalViewSave(mWorkInterval);
            }
        });
    }

    public void setWorkInterval(WorkInterval interval) {
        mWorkInterval = interval;
        mWorkInterval.setViewUpdater(this);
        Log.i("WorkIntervalView", "mWorkInterval=" + Integer.toHexString(System.identityHashCode(mWorkInterval)));
        mEndOn.setEnabled(mWorkInterval.getEndDate() != 0L);
        updateHour(); updateMinute(); updateStartsOn(); updateEndsOn();
        mSave.setEnabled(interval.getStartDate() < interval.getEndDate());
    }

    public void clearEndOn() {
        mEndOn.getText().clear();
    }

    public boolean performSave() {
        if (mSave.isEnabled()) {
            mSave.performClick();
            return true;
        }
        return false;
    }

    // This only updates the start date and end date fields.
    @Override
    public void updateView(int which) {
        switch (which) {
            case DateTimeSettable.SET_START:
                updateStartsOn();
                if (!mEndOn.isEnabled()) {
                    mEndOn.setEnabled(mStartOn.getText().length() != 0);
                }
                updateEndsOn();
                break;
            case DateTimeSettable.SET_END:
                updateEndsOn();
                updateDuration();
                break;
            default:
                break;
        }
        mSave.setEnabled(mWorkInterval.getStartDate() < mWorkInterval.getEndDate());
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
