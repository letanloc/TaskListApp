package com.gmail.philliphsudev.progress;

import com.gmail.philliphsudev.progress.model.Task;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Phillip Hsu on 9/4/2015.
 */
public class ComparatorUtils {

    private static final HashMap<Integer, Comparator<Task>> sComparators = new HashMap<>();

    public static final int KEY_IMPORTANCE              = 0;
    public static final int KEY_NAME                    = 1;
    public static final int KEY_ORDER_CREATED           = 2;
    public static final int KEY_DATE_COMPLETED          = 3;
    public static final int KEY_DUE_DATE                = 4;
    public static final int KEY_REV_IMPORTANCE          = 5;
    public static final int KEY_REV_NAME                = 6;
    public static final int KEY_REV_ORDER_CREATED       = 7;
    public static final int KEY_REV_DATE_COMPLETED      = 8;
    public static final int KEY_REV_DUE_DATE            = 9;
    public static final int KEY_MY_ORDER                = 10;

    public static final Comparator<Task> IMPORTANCE = new ImportanceComparator();
    public static final Comparator<Task> NAME = new NameComparator();
    public static final Comparator<Task> ORDER_CREATED = new OrderCreatedComparator();
    public static final Comparator<Task> DATE_COMPLETED = new DateCompletedComparator();
    public static final Comparator<Task> DUE_DATE = new DueDateComparator();
    public static final Comparator<Task> REV_IMPORTANCE = Collections.reverseOrder(IMPORTANCE);
    public static final Comparator<Task> REV_NAME = Collections.reverseOrder(NAME);
    public static final Comparator<Task> REV_ORDER_CREATED = Collections.reverseOrder(ORDER_CREATED);
    public static final Comparator<Task> REV_DATE_COMPLETED = Collections.reverseOrder(DATE_COMPLETED);
    public static final Comparator<Task> REV_DUE_DATE = Collections.reverseOrder(DUE_DATE);
    public static final Comparator<Task> MY_ORDER = new MyOrderComparator();

    static {
        sComparators.put(KEY_IMPORTANCE, IMPORTANCE);
        sComparators.put(KEY_NAME, NAME);
        sComparators.put(KEY_ORDER_CREATED, ORDER_CREATED);
        sComparators.put(KEY_DATE_COMPLETED, DATE_COMPLETED);
        sComparators.put(KEY_DUE_DATE, DUE_DATE);
        sComparators.put(KEY_REV_IMPORTANCE, REV_IMPORTANCE);
        sComparators.put(KEY_REV_NAME, REV_NAME);
        sComparators.put(KEY_REV_ORDER_CREATED, REV_ORDER_CREATED);
        sComparators.put(KEY_REV_DATE_COMPLETED, REV_DATE_COMPLETED);
        sComparators.put(KEY_REV_DUE_DATE, REV_DUE_DATE);
        sComparators.put(KEY_MY_ORDER, MY_ORDER);
    }

    public static Comparator<Task> valueOf(int i) {
        return sComparators.get(i);
    }

    public static int intKey(Comparator<Task> c) {
        for (Map.Entry<Integer, Comparator<Task>> entry : sComparators.entrySet())
            if (entry.getValue().equals(c))
                return entry.getKey();
        return -1;
    }

    private static final class ImportanceComparator implements Comparator<Task> {
        @Override
        public int compare(Task lhs, Task rhs) {
            boolean b1 = lhs.isImportant();
            boolean b2 = rhs.isImportant();
            // i.e. true ---> false
            return b1 && b2 ? 0 : (b1 && !b2 ? -1 : 1);
        }
    }

    private static final class NameComparator implements Comparator<Task> {
        @Override
        public int compare(Task lhs, Task rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    }

    private static final class OrderCreatedComparator implements Comparator<Task> {
        @Override
        public int compare(Task lhs, Task rhs) {
            return lhs.createdOn() < rhs.createdOn() ? -1 :
                    (lhs.createdOn() > rhs.createdOn() ? 1 : 0);
        }
    }

    private static final class DateCompletedComparator implements Comparator<Task> {
        @Override
        public int compare(Task lhs, Task rhs) {
            // Tasks completed more recently ordered first
            return lhs.completedOn() < rhs.completedOn() ? 1 :
                    (lhs.completedOn() > rhs.completedOn() ? -1 : 0);
        }
    }

    private static final class DueDateComparator implements Comparator<Task> {
        @Override
        public int compare(Task lhs, Task rhs) {
            return lhs.dueOn() < rhs.dueOn() ? -1 :
                    (lhs.dueOn() == rhs.dueOn() ? 0 : 1);
        }
    }

    private static final class MyOrderComparator implements Comparator<Task> {
        @Override
        public int compare(Task lhs, Task rhs) {
            return lhs.getMyPosition() < rhs.getMyPosition() ? -1 :
                    (lhs.getMyPosition() > rhs.getMyPosition() ? 1 : 0);
        }
    }
}
