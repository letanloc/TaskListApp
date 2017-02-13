package com.gmail.philliphsudev.progress.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Phillip Hsu on 6/18/2015.
 */
public class Task implements Parcelable {

    private String mName;
    private boolean mCompleted;
    private boolean mImportant;
    private long mDueOn;
    private final long mDateCreated;
    private List<History> mHistories; // TreeSet?
    private List<WorkInterval> mSchedule; // TreeSet?
    private int mTaskListIndex = -1;
    private long mDateCompleted;
    private int mMyPosition; // When the user wants to sort the task list in his order

    public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel source) {
            return new Task(source);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    public Task() {
        this("");
    }

    public Task(String name) {
        this(name, false, false, 0L);
    }

    public Task(String name, boolean completed, boolean important, long dueOn) {
        mDateCreated = System.currentTimeMillis();
        mName = name;
        mCompleted = completed;
        mImportant = important;
        mDueOn = dueOn;
        mHistories = new ArrayList<>();
        mSchedule = new ArrayList<>();
    }

    // Copy constructor
    public Task(Task rhs) {
        mName = rhs.mName;
        mCompleted = rhs.mCompleted;
        mImportant = rhs.mImportant;
        mDueOn = rhs.mDueOn;
        for (History history : rhs.mHistories) {
            // Not necessary to use the addHistory() method defined in this class, because
            // the rhs Task's list of histories should already be sorted.
            mHistories.add(new History(history));
        }
        for (WorkInterval wi : rhs.mSchedule) {
            mSchedule.add(new WorkInterval(wi));
        }
        mDateCreated = rhs.mDateCreated;
        mTaskListIndex = rhs.mTaskListIndex;
        mDateCompleted = rhs.mDateCompleted;
        mMyPosition = rhs.mMyPosition;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public boolean isCompleted() {
        return mCompleted;
    }

    public void setCompleted(boolean completed) {
        if (completed) {
            mDateCompleted = System.currentTimeMillis();
        } else {
            mDateCompleted = 0L;
        }
        mCompleted = completed;
    }

    public boolean isImportant() {
        return mImportant;
    }

    public void setImportant(boolean important) {
        mImportant = important;
    }

    public long dueOn() {
        return mDueOn;
    }

    public void setDueOn(long dueOn) {
        mDueOn = dueOn;
    }

    public List<History> getHistories() {
        return mHistories;
    }
    
    public void setHistories(List<History> histories) {
        mHistories = histories;
    }

    public List<WorkInterval> getSchedule() {
        return mSchedule;
    }

    public void setSchedule(List<WorkInterval> schedule) {
        mSchedule = schedule;
    }

    public int getTaskListIndex() {
        return mTaskListIndex;
    }

    public void setTaskListIndex(int taskListIndex) {
        mTaskListIndex = taskListIndex;
    }

    public int numHistories() {
        return mHistories.size();
    }

    public int addHistory(History history) {
        mHistories.add(history);
        return sortAndReturnPosition(mHistories, history);
    }

    public int replaceHistory(int position, History history) {
        mHistories.set(position, history);
        return sortAndReturnPosition(mHistories, history);
    }

    public History getHistory(int i) {
        return mHistories.get(i);
    }

    public int numWorkIntervals() {
        return mSchedule.size();
    }

    public int addWorkInterval(WorkInterval wi) {
        mSchedule.add(wi);
        return sortAndReturnPosition(mSchedule, wi);
    }

    public int replaceWorkInterval(int at, WorkInterval wi) {
        mSchedule.set(at, wi);
        return sortAndReturnPosition(mSchedule, wi);
    }

    public void deleteWorkInterval(int at) {
        mSchedule.remove(at);
    }

    public WorkInterval getWorkInterval(int at) {
        return mSchedule.get(at);
    }

    public long createdOn() {
        return mDateCreated;
    }

    public long completedOn() {
        return mDateCompleted;
    }

    public int getMyPosition() {
        return mMyPosition;
    }

    public void setMyPosition(int myPosition) {
        mMyPosition = myPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Task))
            return false;
        Task rhs = (Task) o;
        return mDateCreated == rhs.mDateCreated;
                /*&& mMyPosition == rhs.mMyPosition
                && mDateCompleted == rhs.mDateCompleted
                && mTaskListIndex == rhs.mTaskListIndex
                && mName.equals(rhs.mName)
                && mIndexInList == rhs.mIndexInList
                && mCompleted == rhs.mCompleted
                && mImportant == rhs.mImportant
                && mDueOn == rhs.mDueOn
                && mHistories.equals(rhs.mHistories)
                && mSchedule.equals(rhs.mSchedule);*/

    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (int) (mDateCreated ^ (mDateCreated >>> 32));
        /*result = 31 * result + mIndexInList;
        result = 31 * result + (int) (mDateCompleted ^ (mDateCompleted >>> 32));
        result = 31 * result + (mCompleted ? 1 : 0);
        result = 31 * result + (mImportant ? 1 : 0);
        result = 31 * result + (int) (mDueOn ^ (mDueOn >>> 32));
        result = 31 * result + mName.hashCode();
        result = 31 * result + mHistories.hashCode();
        result = 31 * result + mSchedule.hashCode();
        result = 31 * result + mTaskListIndex;
        result = 31 * result + mMyPosition;*/
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeInt(mCompleted ? 1 : 0); // If true, write 1
        dest.writeInt(mImportant ? 1 : 0);
        dest.writeLong(mDueOn);
        dest.writeLong(mDateCreated);
        dest.writeTypedList(mHistories);
        dest.writeTypedList(mSchedule);
        dest.writeInt(mTaskListIndex);
        dest.writeLong(mDateCompleted);
        dest.writeInt(mMyPosition);
    }

    private int sortAndReturnPosition(List<? extends Comparable> list, Comparable item) {
        Collections.sort(list);
        return list.indexOf(item);
    }

    private Task(Parcel in) {
        mName = in.readString();
        mCompleted = in.readInt() != 0; // mIsCompleted == true if retrieved int != 0
        mImportant = in.readInt() != 0;
        mDueOn = in.readLong();
        mDateCreated = in.readLong();
        mHistories = in.createTypedArrayList(History.CREATOR);
        mSchedule = in.createTypedArrayList(WorkInterval.CREATOR);
        mTaskListIndex = in.readInt();
        mDateCompleted = in.readLong();
        mMyPosition = in.readInt();
    }

}
