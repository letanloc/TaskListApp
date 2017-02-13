package com.gmail.philliphsudev.progress;

import android.os.Parcel;
import android.os.Parcelable;

import com.gmail.philliphsudev.progress.model.History;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Phillip Hsu on 7/26/2015.
 */
public abstract class AbstractTask implements Parcelable, Comparable<AbstractTask> {

    private String mName;
    private boolean mCompleted;
    private boolean mImportant;
    private long mDueOn;
    private final List<History> mHistories;
    private Comparator mHistoryComparator;
    private int mLastPosition = -1;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeInt(mCompleted ? 1 : 0); // If true, write 1
        dest.writeInt(mImportant ? 1 : 0);
        dest.writeTypedList(mHistories);
        dest.writeLong(mDueOn);
        dest.writeInt(mLastPosition);
    }

    protected AbstractTask(Parcel in) {
        mName = in.readString();
        mCompleted = in.readInt() != 0; // mIsCompleted == true if retrieved int != 0
        mImportant = in.readInt() != 0;
        mHistories = in.createTypedArrayList(History.CREATOR);
        mDueOn = in.readLong();
        mLastPosition = in.readInt();
    }

    protected AbstractTask() {
        this("");
    }

    protected AbstractTask(String name) {
        this(name, false, false, 0L);
    }

    protected AbstractTask(String name, boolean completed, boolean important, long dueOn) {
        mName = name;
        mCompleted = completed;
        mImportant = important;
        mDueOn = dueOn;
        mHistories = new ArrayList<>();
    }

    protected AbstractTask(AbstractTask other) {
        this(other.mName, other.mCompleted, other.mImportant, other.mDueOn);
        for (History history : other.mHistories) {
            // Not necessary to use the addHistory() method defined in this class, because
            // the other Task's list of histories should already be sorted.
            mHistories.add(new History(history));
        }
        this.mLastPosition = other.mLastPosition;
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
        mCompleted = completed;
    }

    public boolean isImportant() {
        return mImportant;
    }

    public void setImportant(boolean important) {
        mImportant = important;
    }

    public long dueWhen() {
        return mDueOn;
    }

    public void setDueOn(long dueOn) {
        mDueOn = dueOn;
    }

    public int getLastPosition() {
        return mLastPosition;
    }

    public void setLastPosition(int lastPosition) {
        this.mLastPosition = lastPosition;
    }

    public List<History> getHistories() {
        /*List<History> copy = new ArrayList<>();
        for (History h : mHistories) {
            copy.add(new History(h));
        }
        return copy;*/
        return Collections.unmodifiableList(mHistories);
    }

    public int numHistories() {
        return mHistories.size();
    }

    public int addHistory(History history) {
        mHistories.add(history);
        sortHistory();
        return mHistories.indexOf(history);
    }

    public int replaceHistory(int position, History history) {
        try {
            mHistories.set(position, history);
            sortHistory();
            return mHistories.indexOf(history);
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("Invalid index; cannot replace History");
        }
    }

    public boolean deleteHistory(History h) {
        return mHistories.remove(h);
    }

    public History deleteHistory(int i) {
        return mHistories.remove(i);
    }

    public History getHistory(int i) {
        return mHistories.get(i);
    }

    @Override
    public int compareTo(AbstractTask another) {
        return this.mDueOn < another.mDueOn ? -1 :
                (this.mDueOn == another.mDueOn ? 0 : 1);
    }

    // Only used to determine which order (ascending or descending)
    // Null comparator means default order of descending
    public Comparator getHistoryComparator() {
        return mHistoryComparator;
    }

    // I'd only use this to set a reverse-order comparator
    public void setHistoryComparator(Comparator historyComparator) {
        mHistoryComparator = historyComparator;
        sortHistory();
    }

    public static final class ImportanceComparator implements Comparator<AbstractTask> {
        @Override
        public int compare(AbstractTask lhs, AbstractTask rhs) {
            boolean b1 = lhs.mImportant;
            boolean b2 = rhs.mImportant;
            // i.e. true ---> false
            return b1 && b2 ? 0 : (b1 && !b2 ? -1 : 1);
        }
    }

    public static final class NameComparator implements Comparator<AbstractTask> {
        @Override
        public int compare(AbstractTask lhs, AbstractTask rhs) {
            return lhs.mName.compareTo(rhs.mName);
        }
    }

    private void sortHistory() {
        if (mHistoryComparator == null)
            Collections.sort(mHistories);
        else Collections.sort(mHistories, mHistoryComparator);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AbstractTask))
            return false;
        AbstractTask rhs = (AbstractTask) o;
        return mLastPosition == rhs.mLastPosition
                && mCompleted == rhs.mCompleted
                && mImportant == rhs.mImportant
                && mDueOn == rhs.mDueOn
                && mName.equals(rhs.mName)
                && mHistories.equals(rhs.mHistories);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + mLastPosition;
        result = 31 * result + (mCompleted ? 1 : 0);
        result = 31 * result + (mImportant ? 1 : 0);
        result = 31 * result + (int) (mDueOn ^ (mDueOn >>> 32));
        result = 31 * result + mName.hashCode();
        result = 31 * result + mHistories.hashCode();
        return result;
    }
}
