package com.gmail.philliphsudev.progress;

import com.gmail.philliphsudev.progress.model.AllTaskList;
import com.gmail.philliphsudev.progress.model.CompletedTaskList;
import com.gmail.philliphsudev.progress.model.CustomTaskList;
import com.gmail.philliphsudev.progress.model.History;
import com.gmail.philliphsudev.progress.model.ImportantTaskList;
import com.gmail.philliphsudev.progress.model.PrimaryTaskList;
import com.gmail.philliphsudev.progress.model.Task;
import com.gmail.philliphsudev.progress.model.TaskList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Phillip Hsu on 7/20/2015.
 */
public class TaskListDb {

    private static final List<TaskList> sTaskLists = new ArrayList<>();
    private static final AllTaskList sAll = new AllTaskList();
    private static final ImportantTaskList sImportant = new ImportantTaskList();
    private static final CompletedTaskList sCompleted = new CompletedTaskList();

    // A collection of all UNIQUE instances of adapters. Now whenever there are changes to the db,
    // we can update them here directly.
    private static final Set<TaskListAdapter> sTaskListAdapters = new HashSet<>();
    private static final TaskAdapter sTaskAdapter = new TaskAdapter(sAll);

    static {
        sTaskLists.add(sAll);
        sTaskLists.add(sImportant);
        sTaskLists.add(sCompleted);

        // Dummy TaskLists
        for (int i = 0; i < 1; i++) {
            CustomTaskList list = new CustomTaskList("Default Task List #" + i, -1);
            sTaskLists.add(list);
            // For now, add some fake tasks to every TaskList we create
            for (int k = 0; k < 11; k++) {
                Task task = new Task("This is a task #" + k, k % 2 == 0, k % 2 == 0,
                        k % 2 == 0 ? System.currentTimeMillis() : 0L);
                for (int j = 0; j < 15; j++) {
                    // For each task, add some histories
                    task.addHistory(new History());
                }
                list.addTask(task);
                task.setTaskListIndex(list.getIndex());
            }
            // Add some important tasks
            for (int a = 0; a < 5; a++) {
                Task task = new Task("Important #" + a, false, true, 0L);
                task.setTaskListIndex(list.getIndex());
                list.addTask(task);
            }
        }
    }

    public static final List<TaskList> lists(boolean excludePrimaries) {
        if (excludePrimaries)
            return sTaskLists.subList(PrimaryTaskList.howMany(), sTaskLists.size());
        return sTaskLists;
    }

    public static final int size() {
        return sTaskLists.size();
    }

    public static int addList(CustomTaskList list) {
        sTaskLists.add(list);
        for (TaskListAdapter adapter : sTaskListAdapters) {
            adapter.notifyItemInserted(size() - 1);
        }
        return size() - 1;
    }

    public static void removeList(int at) {
        if (at < PrimaryTaskList.howMany()) {
            throw new IllegalArgumentException("Cannot delete a PrimaryTaskList");
        }
        CustomTaskList list = (CustomTaskList) sTaskLists.remove(at);
        updateIndices(at);
        for (TaskListAdapter adapter : sTaskListAdapters) {
            adapter.notifyItemRemoved(at);
        }
        sAll.removeAllTasks(list.getTasks());
        sImportant.removeAllTasks(list.getTasks()); // Removes only tasks that are important
        sCompleted.removeAllTasks(list.getCompletedTasks());
    }

    public static TaskList getList(int i) {
        return sTaskLists.get(i);
    }

    public static void moveLists(int from, int to) {
        if (from < PrimaryTaskList.howMany() || to < PrimaryTaskList.howMany())
            throw new IllegalArgumentException("Cannot move lists at these indices: " + from + ", " + to);
        CustomTaskList one = (CustomTaskList) sTaskLists.get(from);
        CustomTaskList two = (CustomTaskList) sTaskLists.get(to);
        for (Task task : one.getTasks())
            task.setTaskListIndex(to);
        for (Task task : one.getCompletedTasks())
            task.setTaskListIndex(to);
        for (Task task : two.getTasks())
            task.setTaskListIndex(from);
        for (Task task : two.getCompletedTasks())
            task.setTaskListIndex(from);
        one.setIndex(to);
        two.setIndex(from);
        Collections.swap(sTaskLists, from, to);
        for (TaskListAdapter adapter : sTaskListAdapters) {
            adapter.notifyItemMoved(from, to);
        }
    }

    public static void updateList(int at) {
        if (at < PrimaryTaskList.howMany()) {
            throw new IllegalArgumentException("Cannot update a PrimaryTaskList");
        }
        for (TaskListAdapter adapter : sTaskListAdapters) {
            adapter.notifyItemChanged(at);
        }
    }

    public static final int indexOf(TaskList list) {
        return sTaskLists.indexOf(list);
    }

    public static void addTaskToPrimaries(Task task) {
        if (task.isCompleted()) {
            int at = sCompleted.addTask(task);
            if (sTaskAdapter.currentTaskList() == sCompleted) {
                //sTaskAdapter.notifyItemInserted(at);
                sTaskAdapter.notifyDataSetChanged();
            }
        } else {
            if (task.isImportant()) {
                int at = sImportant.addTask(task);
                if (sTaskAdapter.currentTaskList() == sImportant) {
                    //sTaskAdapter.notifyItemInserted(at);
                    sTaskAdapter.notifyDataSetChanged();
                }
            }
            int at = sAll.addTask(task);
            if (sTaskAdapter.currentTaskList() == sAll) {
                //sTaskAdapter.notifyItemInserted(at);
                sTaskAdapter.notifyDataSetChanged();
            }
        }
    }

    public static void replaceTaskInPrimaries(Task t1, Task t2) {
        if (t1.isCompleted()) {
            int pos1 = sCompleted.indexOf(t1);
            if (t2.isCompleted()) {
                // Both the original task and the replacement task are completed, so we can
                // replace directly
                int pos2 = sCompleted.replaceTask(pos1, t2);
                if (sTaskAdapter.currentTaskList() == sCompleted) {
                    /*if (pos1 != pos2) {
                        sTaskAdapter.notifyItemMoved(pos1, pos2);
                    }
                    sTaskAdapter.notifyItemChanged(pos2);*/
                    sTaskAdapter.notifyDataSetChanged();
                }
            } else {
                // Original task was completed, but replacement isn't. Delete the original task from
                // 'Completed' and add the replacement to the other primaries.
                sCompleted.deleteTask(pos1);
                if (sTaskAdapter.currentTaskList() == sCompleted) {
                    sTaskAdapter.notifyItemRemoved(pos1);
                }
                if (t2.isImportant()) {
                    int pos2 = sImportant.addTask(t2);
                    if (sTaskAdapter.currentTaskList() == sImportant) {
                        sTaskAdapter.notifyItemInserted(pos2);
                    }
                }
                int pos2 = sAll.addTask(t2);
                if (sTaskAdapter.currentTaskList() == sAll) {
                    sTaskAdapter.notifyItemInserted(pos2);
                }
            }
        } else {
            if (t1.isImportant()) {
                int pos1 = sImportant.indexOf(t1);
                if (t2.isImportant()) {
                    // Both tasks are important, so we can do a direct replacement
                    int pos2 = sImportant.replaceTask(pos1, t2);
                    if (sTaskAdapter.currentTaskList() == sImportant) {
                        /*if (pos1 != pos2) {
                            sTaskAdapter.notifyItemMoved(pos1, pos2);
                        }
                        sTaskAdapter.notifyItemChanged(pos2);*/
                        sTaskAdapter.notifyDataSetChanged();
                    }
                } else {
                    // task1 is important, but task2 isn't. Delete task1 from 'Important'.
                    sImportant.deleteTask(pos1);
                    if (sTaskAdapter.currentTaskList() == sImportant) {
                        sTaskAdapter.notifyItemRemoved(pos1);
                    }
                }
            }
            int pos1 = sAll.indexOf(t1);
            int pos2 = sAll.replaceTask(pos1, t2);
            if (sTaskAdapter.currentTaskList() == sAll) {
                /*if (pos1 != pos2) {
                    sTaskAdapter.notifyItemMoved(pos1, pos2);
                }
                sTaskAdapter.notifyItemChanged(pos2);*/
                sTaskAdapter.notifyDataSetChanged();
            }
        }
    }

    public static void removeTaskFromPrimaries(Task task) {
        if (task.isCompleted()) {
            int at = sCompleted.indexOf(task);
            sCompleted.deleteTask(at);
            if (sTaskAdapter.currentTaskList() == sCompleted) {
                sTaskAdapter.notifyItemRemoved(at);
            }
        } else {
            if (task.isImportant()) {
                int at = sImportant.indexOf(task);
                sImportant.deleteTask(at);
                if (sTaskAdapter.currentTaskList() == sImportant) {
                    sTaskAdapter.notifyItemRemoved(at);
                }
            }
            int at = sAll.indexOf(task);
            sAll.deleteTask(at);
            if (sTaskAdapter.currentTaskList() == sAll) {
                sTaskAdapter.notifyItemRemoved(at);
            }
        }
    }

    public static void clearCompleted() {
        sCompleted.getTasks().clear();
    }

    /*
    public static void addTask(Task task) {
        CustomTaskList list = (CustomTaskList) getList(task.getTaskListIndex());
        int at = list.addTask(task); // Add task to the TaskList specified, and also to any primary TaskLists
        task.setIndexInList(at);
        if (list == sTaskAdapter.currentTaskList()) {
            // Notify that the *current* TaskList displayed needs to be updated with the added task
            sTaskAdapter.notifyItemInserted(at);
        }
    }

    // This method explicitly demands a listIndex because task2 could have changed its
    // task list index. The value of listIndex1 should always be the task's original task list index.
    // Note that you cannot rely on getting the current TaskList from the task adapter and expecting
    // that to be the correct list, because the current TaskList could be a PrimaryTaskList.
    public static void replaceTask(int listIndex1, Task task2) {
        //int listIndex1 = sTaskAdapter.currentTaskList().getIndex();
        int listIndex2 = task2.getTaskListIndex(); // Selected task list
        int pos1 = task2.indexInList(); // Original position in list1

        // True only if both indices refer to the same instance of CustomTaskList
        if (listIndex1 == listIndex2) {
            // Replace task1 at pos1 with task2
            int pos2 = getList(listIndex1).replaceTask(pos1, task2);
            task2.setIndexInList(pos2);
            // It will always be the case that you'll need to update the adapter
            if (pos1 != pos2) {
                sTaskAdapter.notifyItemMoved(pos1, pos2);
            }
            sTaskAdapter.notifyItemChanged(pos2);
        } else {
            // This only executes if the indices refer to different instances of TaskList.
            // They can refer to two different CustomTaskLists, or listIndex1 refers to a
            // PrimaryTaskList and listIndex2 a CustomTaskList
            if (listIndex1 < PrimaryTaskList.howMany()) {
                // Get task1 at pos1 from list1
                Task task1 = getList(listIndex1).getTask(pos1); // Task from the primary list
                // Use list2 to do the replacement, which will subsequently do the same replacement on the primaries
                CustomTaskList list2 = (CustomTaskList) getList(listIndex2);
                //
                // Since task1 comes from a PrimaryTaskList, we must find the "actual" task within the actual CustomTaskList
                //int at = list2.indexOf(task1);
                //
                // In list2, replace task1, located at indexInList(), with task2
                int pos2 = list2.replaceTask(task1.indexInList(), task2);
                task2.setIndexInList(pos2);
                // The TaskAdapter is currently displaying list1, so must notify of changes
                if (pos1 != pos2) {
                    sTaskAdapter.notifyItemMoved(pos1, pos2);
                }
                sTaskAdapter.notifyItemChanged(pos2);
                // At this point, list2 can NEVER be in view so no need to notify adapter
            } else {
                // listIndex1 > PrimaryTaskList.howMany()
                getList(listIndex1).deleteTask(pos1); // Delete from the CustomTaskList and the primaries
                // The TaskAdapter is currently displaying list1, so must notify of removal
                sTaskAdapter.notifyItemRemoved(pos1);
                int pos2 = getList(listIndex2).addTask(task2); // Takes care of adding to primaries, even if completed
                task2.setIndexInList(pos2);
                // At this point, list2 can NEVER be in view so no need to notify adapter
            }
        }
    }

    // This method explicitly demands a listIndex because a task in-editing could have changed its
    // task list index. The value of listIndex should always be the task's original task list index.
    // Note that you cannot rely on getting the current TaskList from the task adapter and expecting
    // that to be the correct list, because the current TaskList could be a PrimaryTaskList.
    public static void deleteTask(int listIndex, int at) {
        TaskList currList = sTaskAdapter.currentTaskList();
        if (currList instanceof PrimaryTaskList) {
            Task task = currList.getTask(at);
            getList(listIndex).deleteTask(task.indexInList()); // Delete from the actual CustomTaskList and the primaries
            // Need to update the primary task list, but NOT the actual CustomTaskList (since it's not in view)
            //sTaskAdapter.notifyItemRemoved(at); // For some reason, not correct...
            return;
        }
        // We can delete by position because listIndex will ALWAYS refer to the currently displayed
        // TaskList in the task adapter.
        getList(listIndex).deleteTask(at); // Delete from CustomTaskList and the primaries
        // Update the CustomTaskList, but NOT for the primaries
        sTaskAdapter.notifyItemRemoved(at);
    }
    */

    // Updates the positions (i.e. last positions) of TaskLists
    private static void updateIndices(int startingAt) {
        if (startingAt < PrimaryTaskList.howMany())
            throw new IllegalArgumentException("Cannot update TaskList indices starting from a PrimaryTaskList");
        int n = startingAt;
        for (; n < sTaskLists.size(); n++) {
            ((CustomTaskList)getList(n)).setIndex(n);
        }
    }

    public static void registerTaskListAdapter(TaskListAdapter adapter) {
        sTaskListAdapters.add(adapter);
    }

    public static void unregisterTaskListAdapter(TaskListAdapter adapter) {
        sTaskListAdapters.remove(adapter);
    }

    public static TaskAdapter getTaskAdapter() {
        return sTaskAdapter;
    }

    public static boolean isPrimary(int at) {
        return at >= 0 && at < PrimaryTaskList.howMany();
    }
}
