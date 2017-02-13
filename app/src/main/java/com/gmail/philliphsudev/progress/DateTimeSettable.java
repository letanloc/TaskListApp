package com.gmail.philliphsudev.progress;

/**
 * Created by Phillip Hsu on 8/19/2015.
 */
public interface DateTimeSettable {

    // Markers to be passed to SetDateTimeDialog, so that it can modify
    // the appropriate field for the object that implements that interface
    int SET_START = 0;
    int SET_END = 1;

    void setStartDate(long startDate);

    void setEndDate(long endDate);

    long getStartDate();

    long getEndDate();
}
