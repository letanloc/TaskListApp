package com.gmail.philliphsudev.progress.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.gmail.philliphsudev.progress.AbstractTask;

/**
 * Created by Phillip Hsu on 7/26/2015.
 */
public class Subtask extends AbstractTask {

    // Don't know if I'll actually do leveled subtasks....
    private int mLevel;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(mLevel);
    }

    public static final Parcelable.Creator<Subtask> CREATOR = new Parcelable.Creator<Subtask>() {
        @Override
        public Subtask createFromParcel(Parcel source) {
            return new Subtask(source);
        }

        @Override
        public Subtask[] newArray(int size) {
            return new Subtask[size];
        }
    };

    private Subtask(Parcel in) {
        super(in);
        mLevel = in.readInt();
    }

    public Subtask() {
        super();
    }

    public Subtask(String name) {
        super(name);
    }

    public Subtask(String name, boolean completed, boolean important, long dueOn) {
        super(name, completed, important, dueOn);
    }

    public Subtask(Subtask rhs) {
        super(rhs);
        mLevel = rhs.mLevel;
    }

}
