package com.gmail.philliphsudev.progress.model;

import android.os.Parcel;

/**
 * Created by Phillip Hsu on 9/16/2015.
 */
public class AllTaskList extends PrimaryTaskList {

    private static final String NAME = "All";
    private static final int COLOR = 0;

    public AllTaskList() {
        super(NAME, COLOR);
    }

    public static final Creator<AllTaskList> CREATOR = new Creator<AllTaskList>() {
        @Override
        public AllTaskList createFromParcel(Parcel source) {
            return new AllTaskList(source);
        }

        @Override
        public AllTaskList[] newArray(int size) {
            // There can never be more than one instance of AllTaskList
            throw new UnsupportedOperationException();
        }
    };
    
    private AllTaskList(Parcel in) {
        super(in);
    }
}
