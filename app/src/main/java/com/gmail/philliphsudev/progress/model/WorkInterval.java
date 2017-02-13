package com.gmail.philliphsudev.progress.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.gmail.philliphsudev.progress.DateTimeSettable;

/**
 * Created by Phillip Hsu on 8/17/2015.
 */
public class WorkInterval implements Parcelable, Comparable<WorkInterval>, DateTimeSettable {

    private String mDescription;
    private int mHours = 0;
    private int mMinutes = 0;
    private long mStartOn = 0L;
    private long mEndOn = 0L;
    private int mLastPosition = -1;

    private ViewUpdater mListener;

    // A listener that prompts the WorkIntervalView that corresponds to this WorkInterval
    // to update any relevant views when the start or end date is set/changed. (Not needed
    // for the hour and minute views)
    public interface ViewUpdater {
        void updateView(int which); // Updates the start date and end date fields.
    }

    public void setViewUpdater(ViewUpdater updater) {
        mListener = updater;
    }

    public WorkInterval() {

    }

    public WorkInterval(String desc, int hours, int minutes, long startOn) {
        mDescription = desc;
        mHours = hours;
        mMinutes = minutes;
        mStartOn = startOn;
        computeEndOn();
    }

    public WorkInterval(WorkInterval other) {
        mDescription = other.mDescription;
        mHours = other.mHours;
        mMinutes = other.mMinutes;
        mStartOn = other.mStartOn;
        mEndOn = other.mEndOn;
        mLastPosition = other.mLastPosition;
    }

    public WorkInterval(Parcel in) {
        mDescription = in.readString();
        mHours = in.readInt();
        mMinutes = in.readInt();
        mStartOn = in.readLong();
        mEndOn = in.readLong();
        mLastPosition = in.readInt();
    }

    public int getHours() {
        return mHours;
    }

    public void setHours(int hours) {
        mHours = hours;
    }

    public int getMinutes() {
        return mMinutes;
    }

    public void setMinutes(int minutes) {
        mMinutes = minutes;
    }

    public int getLastPosition() {
        return mLastPosition;
    }

    public void setLastPosition(int lastPosition) {
        mLastPosition = lastPosition;
    }

    @Override
    public long getStartDate() {
        return mStartOn;
    }

    @Override
    public void setStartDate(long startOn) {
        mStartOn = startOn;
        computeEndOn();
        /*if (mListener != null)
            mListener.updateView(DateTimeSettable.SET_START);
        else throw new NullPointerException("This WorkInterval does not have a ViewUpdater set");*/
    }

    @Override
    public long getEndDate() {
        return mEndOn;
    }

    @Override
    public void setEndDate(long endOn) {
        mEndOn = endOn;
        computeDuration();
        /*if (mListener != null)
            mListener.updateView(DateTimeSettable.SET_END);*/
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public void incrementHour() {
        mHours++;
        computeEndOn();
    }

    public void decrementHour() {
        // Try subtracting one hour (in msecs) from the current endOn value
        boolean ableToDecrease = mEndOn - 3600000 >= mStartOn;
        if (mHours > 0 && (ableToDecrease || (mEndOn == 0L && mStartOn == 0L)) ) {
            mHours--;
            computeEndOn();
        }
    }

    public void incrementMinutes() {
        // Increment by 5 mins
        mMinutes += 5;
        computeEndOn();
    }

    public void decrementMinutes() {
        if (mMinutes >= 5) {
            // Try subtracting 5 minutes (in msecs) from current endOn value
            boolean ableToDecrease = mEndOn - 300000 >= mStartOn;
            // Also allow decrement if minutes were set but start or end dates have yet to be set
            if (ableToDecrease || (mEndOn == 0L && mStartOn == 0L)) {
                mMinutes -= 5;
            }
        } else {
            // Try subtracting whatever minutes remains, but check for negatives?
            boolean able = mEndOn - (mMinutes * 60000) >= mStartOn;
            if (mMinutes >= 0 && mMinutes < 5 && able) {
                mMinutes = 0;
            }
        }

        computeEndOn();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WorkInterval))
            return false;
        WorkInterval rhs = (WorkInterval) o;
        return mHours == rhs.mHours
                && mMinutes == rhs.mMinutes
                && mStartOn == rhs.mStartOn
                && mEndOn == rhs.mEndOn
                && mDescription.equals(rhs.mDescription);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + mDescription.hashCode();
        result = 31 * result + mHours;
        result = 31 * result + mMinutes;
        result = 31 * result + (int) (mStartOn ^ (mStartOn >>> 32));
        result = 31 * result + (int) (mEndOn ^ (mEndOn >>> 32));
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mDescription);
        dest.writeInt(mHours);
        dest.writeInt(mMinutes);
        dest.writeLong(mStartOn);
        dest.writeLong(mEndOn);
        dest.writeInt(mLastPosition);
    }

    public static final Parcelable.Creator<WorkInterval> CREATOR = new Creator<WorkInterval>() {
        @Override
        public WorkInterval createFromParcel(Parcel source) {
            return new WorkInterval(source);
        }

        @Override
        public WorkInterval[] newArray(int size) {
            return new WorkInterval[size];
        }
    };

    @Override
    public int compareTo(WorkInterval another) {
        return mStartOn > another.mStartOn ? 1 : (mStartOn < another.mStartOn ? -1 : 0);
    }

    private void computeEndOn() {
        if (mStartOn == 0L) {
            return;
        }
        int msecs = (mHours * 3600 + mMinutes * 60) * 1000;
        mEndOn = mStartOn + msecs;
    }

    private void computeDuration() {
        long msecs = mEndOn - mStartOn;
        double hours = (double) msecs / 3600000;
        double fractionalHours = hours % 1;
        int integerHours = (int) (hours - fractionalHours);
        mHours = integerHours;
        mMinutes = (int) Math.round(fractionalHours * 60);
    }

}
