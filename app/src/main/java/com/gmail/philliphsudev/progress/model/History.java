package com.gmail.philliphsudev.progress.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Phillip Hsu on 8/23/2015.
 */
public class History implements Parcelable, Comparable<History> {

    private final String mTag; // If an ActionItem was timed, then this stores its name
    private List<Event> mEvents;
    private int mLastPosition = -1;

    public History() {
        this("");
    }

    public History(String tag) {
        mTag = tag;
        mEvents = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            mEvents.add(new Event(System.currentTimeMillis() * (i+1), i % 4));
        }
    }

    public History(History rhs) {
        mTag = rhs.mTag;
        mEvents = new ArrayList<>();
        for (Event e : rhs.mEvents) {
            mEvents.add(new Event(e));
        }
        mLastPosition = rhs.mLastPosition;
    }

    private History(Parcel in) {
        mTag = in.readString();
        mEvents = in.createTypedArrayList(Event.CREATOR);
        mLastPosition = in.readInt();
    }

    public int getLastPosition() {
        return mLastPosition;
    }

    public void setLastPosition(int lastPosition) {
        mLastPosition = lastPosition;
    }

    public String getTag() {
        return mTag;
    }

    public List<Event> getEvents() {
        return mEvents;
    }

    public void setEvents(List<Event> events) {
        mEvents = events;
    }

    // This isn't much use if Event is a private class
    public Event getEventAt(int at) {
        return mEvents.get(at);
    }

    public long getEventTime(int at) {
        return mEvents.get(at).mTime;
    }

    public String getEventComments(int at) {
        return mEvents.get(at).mComments;
    }

    public int getEventRating(int at) {
        return mEvents.get(at).mRating;
    }

    public boolean addEvent(long time, int state) {
        if (checkIfAddingTimeValid(time) && checkIfAddingStateValid(Event.State.valueOf(state))) {
            mEvents.add(new Event(time, state));
            return true;
        }
        return false;
    }

    public long firstEventTime() {
        if (!mEvents.isEmpty())
            return mEvents.get(0).getTime();
        return -1L;
    }

    public long lastEventTime() {
        if (mEvents.size() > 0) {
            return mEvents.get(mEvents.size() - 1).getTime();
        }
        return -1L;
    }

    @Override
    public int compareTo(History another) {
        // Most recent histories should be at the top, and the earliest at the bottom
        // Flip the signs on the ones if you want to sort the other way
        return lastEventTime() > another.lastEventTime() ? -1 :
                (lastEventTime() < another.lastEventTime() ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTag);
        dest.writeTypedList(mEvents);
        dest.writeInt(mLastPosition);
    }

    public static final Parcelable.Creator<History> CREATOR = new Parcelable.Creator<History>() {
        @Override
        public History createFromParcel(Parcel source) {
            return new History(source);
        }

        @Override
        public History[] newArray(int size) {
            return new History[size];
        }
    };

    private boolean checkIfAddingStateValid(Event.State s) {
        if (mEvents.isEmpty()) {
            // The very first Event must be of state STARTED
            return s == Event.State.STARTED;
        }

        // Get the current last Event's state and compare to the provided state to be added
        Event.State currentLast = mEvents.get(mEvents.size() - 1).getState();

        if (currentLast == Event.State.FINISHED) {
            // Can't add any more Events if the last Event is of state FINISHED
            return false;
        }

        if (currentLast == Event.State.STARTED || currentLast == Event.State.RESUMED)
            return s == Event.State.PAUSED || s == Event.State.FINISHED;
        if (currentLast == Event.State.PAUSED)
            return s == Event.State.RESUMED || s == Event.State.FINISHED;

        return false;
    }
    
    private boolean checkIfAddingTimeValid(long time) {
        if (mEvents.isEmpty()) 
            return true; // The very first Event can be always be added
        long currentLast = mEvents.get(mEvents.size() - 1).getTime();
        return time > currentLast; // The List<Event> can only store times sequentially and in ascending order
    }

    public static final class Event implements Parcelable {
        
        private final long mTime;
        private final State mState;
        private String mComments;
        private int mRating;

        public enum State { STARTED, PAUSED, RESUMED, FINISHED;

            public static State valueOf(int i) {
                switch (i) {
                    case 0:
                        return STARTED;
                    case 1:
                        return PAUSED;
                    case 2:
                        return RESUMED;
                    case 3:
                        return FINISHED;
                    default:
                        throw new IllegalArgumentException(
                                "Specified 'state' index corresponds to no State");
                }
            }
        }

        private Event(long time, int state) {
            mTime = time;
            mState = State.valueOf(state);
            mComments = mState.toString();
            mRating = 0;
        }

        private Event(Event rhs) {
            mTime = rhs.mTime;
            mState = rhs.mState;
            mComments = rhs.mComments;
            mRating = rhs.mRating;
        }

        private Event(Parcel in) {
            mTime = in.readLong();
            mState = State.valueOf(in.readInt());
            mComments = in.readString();
            mRating = in.readInt();
        }

        public long getTime() {
            return mTime;
        }

        public State getState() {
            return mState;
        }

        public String getComments() {
            return mComments;
        }

        public void setComments(String comments) {
            mComments = comments;
        }

        public int getRating() {
            return mRating;
        }

        public void setRating(int rating) {
            mRating = rating;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(mTime);
            dest.writeInt(mState.ordinal());
            dest.writeString(mComments);
            dest.writeInt(mRating);
        }

        public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
            @Override
            public Event createFromParcel(Parcel source) {
                return new Event(source);
            }

            @Override
            public Event[] newArray(int size) {
                return new Event[size];
            }
        };
    }

}
