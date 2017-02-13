package com.gmail.philliphsudev.progress;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Phillip Hsu on 7/18/2015.
 */
public class CalendarUtils {

    private static final String DATE_FORMAT_SAME_YEAR = "E MMM d"; // e.g. Mon Jan 15
    private static final String DATE_FORMAT_DIFF_YEAR = "MMM d y"; // e.g. Jan 15 2017
    private static final String TIME_FORMAT = "h:mm a";

    private static final Calendar calendar = Calendar.getInstance();
    private static final SimpleDateFormat sdf = new SimpleDateFormat();

    private static final int CURRENT_YEAR;
    private static final int CURRENT_MONTH;
    private static final int CURRENT_DAY;

    static {
        CURRENT_YEAR = calendar.get(Calendar.YEAR);
        CURRENT_MONTH = calendar.get(Calendar.MONTH);
        CURRENT_DAY = calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static final String getDateString(long time) {
        if (time == 0L)
            return "";

        calendar.setTimeInMillis(time);
        if (calendar.get(Calendar.YEAR) == CURRENT_YEAR) {
            // First, check if date is relative to now
            if (calendar.get(Calendar.MONTH) == CURRENT_MONTH) {
                if (calendar.get(Calendar.DAY_OF_MONTH) == CURRENT_DAY)
                    return "Today";
                if (calendar.get(Calendar.DAY_OF_MONTH) - CURRENT_DAY == 1)
                    return "Tomorrow";
                if (calendar.get(Calendar.DAY_OF_MONTH) - CURRENT_DAY == -1)
                    return "Yesterday";
            }
            // Date not relative to now, so we get to format it with sdf
            // If we get here, then date year and current year are same
            sdf.applyPattern(DATE_FORMAT_SAME_YEAR);
        } else {
            sdf.applyPattern(DATE_FORMAT_DIFF_YEAR);
        }

        String date = sdf.format(calendar.getTime());
        return date;
    }

    public static final String getTimeString(long time) {
        if (time == 0L)
            return "";
        calendar.setTimeInMillis(time);
        sdf.applyPattern(TIME_FORMAT);
        String timeStr = sdf.format(calendar.getTime());
        return timeStr;
    }

    public static final String getDateTimeString(long time) {
        String date = getDateString(time);
        String timeStr = getTimeString(time);

        if (date.isEmpty() && timeStr.isEmpty())
            return "";
        if (timeStr.isEmpty())
            return date;

        String datetime = date + ", " + timeStr;
        return datetime;
    }

    public static final String getDateRangeString(long start, long end) {
        String startDate = getDateString(start);
        String endDate = getDateString(end);
        if (!startDate.equals(endDate)) {
            // Different dates, so must display as a range
            return startDate + " - " + endDate;
        }
        return startDate;
    }

    public static final String getTimeRangeString(long start, long end) {
        return getTimeString(start) + " - " + getTimeString(end);
    }

    public static final String getDateTimeRangeString(long start, long end) {
        String dateRange = getDateRangeString(start, end);
        String timeRange = getTimeRangeString(start, end);
        return dateRange + ", " + timeRange;
    }
}
