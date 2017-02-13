package com.gmail.philliphsudev.progress.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Phillip Hsu on 8/17/2015.
 */
public class ActionItem implements Parcelable, Comparable<ActionItem> {

    private String mName = "";
    private long mDueBy = 0L;
    private boolean mDone = false;
    private List<WorkInterval> mWorkIntervals = new ArrayList<>();
    private int mLastPosition = -1;

    public int indexOfExpandedWorkInterval() {
        for (WorkInterval i : mWorkIntervals) {
            if (i.getLastPosition() >= 0 && i.getLastPosition() < mWorkIntervals.size()) {
                return i.getLastPosition();
            }
        }
        return -1;
    }

    public int numWorkIntervals() {
        return mWorkIntervals.size();
    }

    public int replaceWorkInterval(int pos, WorkInterval wi) {
        mWorkIntervals.set(pos, wi);
        Collections.sort(mWorkIntervals);
        return mWorkIntervals.indexOf(wi);
    }

    public void deleteWorkInterval(int pos) {
        mWorkIntervals.remove(pos);
    }

    // This is the same thing as the original add method, but we don't sort the list so as to preserve
    // the element at the end.
    public int addWorkIntervalAtEnd(WorkInterval wi) {
        mWorkIntervals.add(wi);
        return mWorkIntervals.size() - 1;
    }

    public ActionItem() {
    }

    public ActionItem(String name, long dueBy) {
        mName = name;
        mDueBy = dueBy;
    }

    public ActionItem(ActionItem other) {
        mName = other.mName;
        mDueBy = other.mDueBy;
        mDone = other.mDone;
        for (WorkInterval interval : other.mWorkIntervals) {
            mWorkIntervals.add(new WorkInterval(interval));
        }
        mLastPosition = other.mLastPosition;
    }

    public ActionItem(Parcel in) {
        mName = in.readString();
        mDueBy = in.readLong();
        mDone = in.readInt() != 0;
        mWorkIntervals = in.createTypedArrayList(WorkInterval.CREATOR);
        mLastPosition = in.readInt();
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public long dueBy() {
        return mDueBy;
    }

    public void setDueBy(long dueBy) {
        mDueBy = dueBy;
    }

    public List<WorkInterval> getWorkIntervals() {
        return mWorkIntervals;
    }

    public void setWorkIntervals(List<WorkInterval> workIntervals) {
        mWorkIntervals = workIntervals;
    }

    public int getLastPosition() {
        return mLastPosition;
    }

    public void setLastPosition(int lastPosition) {
        mLastPosition = lastPosition;
    }

    public boolean isDone() {
        return mDone;
    }

    public void setDone(boolean done) {
        mDone = done;
    }

    public WorkInterval upcomingWorkInterval() {
        Collections.sort(mWorkIntervals);
        if (mWorkIntervals.isEmpty()) return null;
        return mWorkIntervals.get(0);
    }

    public int addWorkInterval(WorkInterval interval) {
        mWorkIntervals.add(interval);
        Collections.sort(mWorkIntervals);
        return mWorkIntervals.indexOf(interval);
    }

    public WorkInterval getWorkInterval(int position) {
        return mWorkIntervals.get(position);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeLong(mDueBy);
        dest.writeInt(mDone ? 1 : 0);
        dest.writeTypedList(mWorkIntervals);
        dest.writeInt(mLastPosition);
    }

    public static final Parcelable.Creator<ActionItem> CREATOR = new Parcelable.Creator<ActionItem>() {
        @Override
        public ActionItem createFromParcel(Parcel source) {
            return new ActionItem(source);
        }

        @Override
        public ActionItem[] newArray(int size) {
            return new ActionItem[size];
        }
    };

    @Override
    public int compareTo(ActionItem another) {
        return mDueBy > another.mDueBy ? 1 : (mDueBy < another.mDueBy ? -1 : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ActionItem))
            return false;
        ActionItem rhs = (ActionItem) o;
        return mLastPosition == rhs.mLastPosition
                && mName.equals(rhs.mName)
                && mDueBy == rhs.mDueBy
                && mDone == rhs.mDone
                && mWorkIntervals.equals(rhs.mWorkIntervals);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + mLastPosition;
        result = 31 * result + mName.hashCode();
        result = 31 * result + (int) (mDueBy ^ (mDueBy >>> 32));
        result = 31 * result + (mDone ? 1 : 0);
        result = 31 * result + mWorkIntervals.hashCode();
        return result;
    }
}
