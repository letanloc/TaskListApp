package com.gmail.philliphsudev.progress.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmail.philliphsudev.progress.ComparatorUtils;
import com.gmail.philliphsudev.progress.ItemTouchHelperCallback;
import com.gmail.philliphsudev.progress.R;
import com.gmail.philliphsudev.progress.RecyclerItemClickListener;
import com.gmail.philliphsudev.progress.TaskAdapter;
import com.gmail.philliphsudev.progress.TaskListAdapter;
import com.gmail.philliphsudev.progress.TaskListDb;
import com.gmail.philliphsudev.progress.model.CompletedTaskList;
import com.gmail.philliphsudev.progress.model.CustomTaskList;
import com.gmail.philliphsudev.progress.model.PrimaryTaskList;
import com.gmail.philliphsudev.progress.model.Task;
import com.gmail.philliphsudev.progress.model.TaskList;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;

public class TaskListFragment extends Fragment {

    private TaskList mTaskList;
    private TaskAdapter mTaskAdapter;
    private TaskListAdapter mTaskListAdapter;
    private ItemTouchHelperCallback mTouchCallback;

    private Toolbar mToolbar;
    private TextView mTitle;
    private RecyclerView mMainRecyclerView;
    private DrawerLayout mDrawerLayout;
    private NavigationView mDrawer;
    private FloatingActionButton mFab;
    private RecyclerView mDrawerRecyclerView;
    private View mCreateListBtn;
    private SubMenu mSortMenu;
    private Menu mMenu;
    private MenuInflater mMenuInflater;

    //private Starter mListener;
    private NewStarter mNewStarter;

    // Maps the enum constant to the resource id of the actual menu item.
    private static final EnumMap<TaskList.MenuOptions, Integer> sMenuOptions =
            new EnumMap<>(TaskList.MenuOptions.class);

    static {
        sMenuOptions.put(TaskList.MenuOptions.SORT, R.id.action_sort);
        sMenuOptions.put(TaskList.MenuOptions.VIEW_COMPLETED_TASKS, R.id.view_completed_tasks);
        sMenuOptions.put(TaskList.MenuOptions.EDIT_TASK_LIST, R.id.edit_task_list);
        sMenuOptions.put(TaskList.MenuOptions.SORT_BY_DUE_DATE, R.id.by_due_date);
        sMenuOptions.put(TaskList.MenuOptions.SORT_BY_IMPORTANCE, R.id.by_importance);
        sMenuOptions.put(TaskList.MenuOptions.SORT_BY_NAME, R.id.by_name);
        sMenuOptions.put(TaskList.MenuOptions.SORT_BY_ORDER_CREATED, R.id.by_order_created);
        sMenuOptions.put(TaskList.MenuOptions.SORT_BY_TASK_LIST, R.id.by_task_list);
        sMenuOptions.put(TaskList.MenuOptions.SORT_BY_MY_ORDER, R.id.my_order);
    }

    // An interface to facilitate starting UI components in this Activity.
    public interface Starter {
        void startQuickAddTask();
        void startTaskActivity(int taskListIndex, Task task);
    }

    public interface NewStarter {
        void startQuickAddTask(int listIndex);
        void startEditTask(int lastPos, Task task);
        void startEditTaskList(int listIndex);
    }

    public static TaskListFragment newInstance() {
        return new TaskListFragment();
    }

    public TaskListFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            //mListener = (Starter) activity;
            mNewStarter = (NewStarter) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Starter");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Retrieve an arbitrary TaskList from the database. Might default
        // to 'All' when doing this for real.
        //mTaskList = TaskListDb.getList(0);
        //mTaskAdapter = new TaskAdapter(mTaskList);
        mTaskAdapter = TaskListDb.getTaskAdapter();
        mTaskList = mTaskAdapter.currentTaskList();
        mTaskListAdapter = new TaskListAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list_v2, container, false);
        // Get the main content layout specified by the 'include' tag
        View mainContent = view.findViewById(R.id.main_content_layout);
        // Initialize the view components inside the main content layout
        mToolbar = (Toolbar) mainContent.findViewById(R.id.toolbar);
        mTitle = (TextView) mainContent.findViewById(R.id.title);
        mMainRecyclerView = (RecyclerView) mainContent.findViewById(R.id.main_recyclerView);
        mFab = (FloatingActionButton) mainContent.findViewById(R.id.fab);
        mFab.setImageResource(android.R.drawable.ic_input_add);
        // Initialize the rest of the view components
        mDrawerRecyclerView = (RecyclerView) view.findViewById(R.id.drawer_recyclerView);
        mDrawer = (NavigationView) view.findViewById(R.id.drawer);
        mDrawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_layout);
        mCreateListBtn = view.findViewById(R.id.add_list_btn);

        mTitle.setText(mTaskList.getName());

        // Setup view
        final AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(mToolbar);
        if (activity.getSupportActionBar() != null) {
            //activity.getSupportActionBar().setTitle(mTaskList.getName());
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mMainRecyclerView.setAdapter(mTaskAdapter);
        mMainRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        mMainRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(activity, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Task task;
                if (!mTaskAdapter.isShowingCompletedTasks()) {
                    task = mTaskList.getTask(position);
                } else {
                    task = ((CustomTaskList) mTaskList).getCompletedTask(position);
                }
                //task.setIndexInList(position); // Track the clicked position
                //mListener.startTaskActivity(mTaskList.getIndex(), mTaskList.getTask(position));
                mNewStarter.startEditTask(position, task);
            }
        }));

        // Set up swipe and drag gestures for the main RecyclerView
        mTouchCallback = new ItemTouchHelperCallback(mTaskAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(mTouchCallback);
        touchHelper.attachToRecyclerView(mMainRecyclerView);

        // Set up drag gesture for drawer RecyclerView
        ItemTouchHelper touchHelper2 = new ItemTouchHelper(new ItemTouchHelperCallback(mTaskListAdapter));
        touchHelper2.attachToRecyclerView(mDrawerRecyclerView);

        mDrawerRecyclerView.setAdapter(mTaskListAdapter);
        mDrawerRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        mDrawerRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(activity, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Log.d("TaskListActivity", "Changing task lists");
                mTaskList = TaskListDb.getList(position);
                // TODO Set TaskList's last position to this clicked position??
                mTaskAdapter.changeTaskList(mTaskList);

                if (isShowingCompletedTasks() && (!(mTaskList instanceof CompletedTaskList))) {
                    toggleViewsBasedOnStatusOfTasks();
                } /*else {
                    // The last TaskList was already displaying the incomplete tasks, so no views need
                    // to be toggled. Just set the adapter to display the new TaskList's incomplete tasks
                    // and notify the data set has changed.
                    mTaskAdapter.toggleTasks(TaskListDb.isCompleted(mTaskList));
                }*/
                manageSupportedMenuOptions();
                mTitle.setText(mTaskList.getName());
                selectSortMenuOption();
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
        }));
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTaskList instanceof PrimaryTaskList) {
                    // Set the target task list to the first custom list (for now)
                    // TODO May want to set it to the user's "default" list, e.g "Phillip Hsu's list"
                    mNewStarter.startQuickAddTask(PrimaryTaskList.howMany());
                } else {
                    // Set the target task list to the current one
                    mNewStarter.startQuickAddTask(mTaskList.getIndex());
                }
            }
        });
        mCreateListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewStarter.startEditTaskList(-1);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        Log.i("TaskListFrag", "onResume");
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_task_list, menu);
        mMenu = menu;
        // Hold onto an instance of the sort submenu, so that we can manually update the
        // current TaskList's sorting option when it is selected from the drawer
        mSortMenu = menu.findItem(R.id.action_sort).getSubMenu();
        manageSupportedMenuOptions(); // Hide/show whatever menu options are disabled/enabled in the current TaskList
        selectSortMenuOption();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isShowingCompletedTasks() && (!(mTaskList instanceof CompletedTaskList))) {
                    // Take us back to incompleted tasks
                    toggleViewsBasedOnStatusOfTasks();
                    return true;
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                    return true;
                }
            case R.id.action_sort:
                return item.hasSubMenu();
            case R.id.by_due_date:
            case R.id.by_order_created:
            case R.id.by_importance:
            case R.id.by_name:
            case R.id.my_order:
            case R.id.by_task_list:
                // TODO by task list in helper method
                return onSortOptionSelected(item);
            case R.id.view_completed_tasks:
            case R.id.action_go_back:
                toggleViewsBasedOnStatusOfTasks();
                return true;
            case R.id.clear_completed_tasks:
                if (mTaskList instanceof CustomTaskList) {
                    ((CustomTaskList) mTaskList).clearCompletedTasks();
                    // Don't need to check if currently displaying completed tasks, since this
                    // menu item can only be clicked if they are already displaying
                    mTaskAdapter.notifyDataSetChanged();
                    return true;
                }
                return false;
            case R.id.edit_task_list:
                mNewStarter.startEditTaskList(mTaskList.getIndex());
                return true;
            default:
                return false;
        }
    }

    public void addTask(Task task) {
        // Get the TaskList selected during editing
        TaskList list = TaskListDb.getList(task.getTaskListIndex());
        // Add to the list and to any primaries (even for 'Completed')
        int at = list.addTask(task);
        if (task.isCompleted() && mTaskList.getIndex() == 2) {
            mTaskAdapter.notifyItemInserted(0); // Completed tasks always added to top
        } else if (mTaskList.getIndex() == 0 || (task.isImportant() && mTaskList.getIndex() == 1)) {
            mTaskAdapter.notifyItemInserted(mTaskList.indexOf(task));
        } else if (mTaskList == list) {
            mTaskAdapter.notifyItemInserted(at);
        }
    }

    public void updateTask(int lastPos, Task task) {
        TaskList list = TaskListDb.getList(task.getTaskListIndex());
        if (mTaskList == list) {
            int at = list.replaceTask(lastPos, task);
        } else {
            int lastPosition = mTaskList.indexOf(task);
            mTaskList.deleteTask(lastPosition);
        }
        if (mTaskList instanceof PrimaryTaskList) {
            mTaskAdapter.notifyDataSetChanged();
        } else if (mTaskList == list) {
            if (lastPos != at) {
                mTaskAdapter.notifyItemMoved(lastPos, at);
            }
            mTaskAdapter.notifyItemChanged(at);
        }
    }

    public void deleteTask(int lastPos, Task task) {
        if (mTaskList instanceof PrimaryTaskList) {
            TaskList list = TaskListDb.getList(task.getTaskListIndex());
            list.deleteTask(task);
            // The primary list might have deleted this task, so update the adapter
            mTaskAdapter.notifyDataSetChanged();
        } else {
            // From EditTaskFrag, a task to be deleted will ALWAYS belong to the current TaskList,
            // regardless if it was edited because no changes could possibly have been committed.
            mTaskList.deleteTask(lastPos);
            mTaskAdapter.notifyItemRemoved(lastPos);
        }
    }

    public void notifyTaskCreated(Task task) {
        /*Log.i("TaskListFrag", "notifyTaskCreated()");
        // For some reason, any newly created TaskLists will automatically update and show in the layout
        // without my needing to do this check. I'm just gonna do it anyway...
        //
        // Check if the TaskListAdapter has an item entry at *layout* position specified by the
        // task's task list index. If not, a newly created TaskList has not been updated to the layout yet.
        if (mDrawerRecyclerView.findViewHolderForLayoutPosition(task.getTaskListIndex()) == null) {
            Log.i("TaskListFrag", "Notifying TaskListAdapter of new TaskList added");
            // A new TaskList was created for the task
            mTaskListAdapter.notifyItemInserted(task.getTaskListIndex());
        }*/
        /*TaskList taskList = TaskListDb.getList(task.getTaskListIndex());
        int at = taskList.addTask(task); // Add task to the TaskList specified, and also to any primary TaskLists
        if (taskList == mTaskList) {
            // Notify that the *current* TaskList displayed needs to be updated with the added task
            Log.d("TaskListFrag", "Task added at " + at);
            mTaskAdapter.notifyItemInserted(at);
        }*/
        // TODO update adapter if tasklist is 'all' or 'important' etc...
    }

    public void notifyTaskCreated(int taskListIndex, Task task) {
        /*TaskList list = TaskListDb.getList(taskListIndex);
        if (list == mTaskList) {
            notifyTaskCreated(task);
        } else {
            list.addTask(task);
        }*/
    }

    public void notifyTaskDeleted(Task task) {
        // TODO: I don't think a Task elected for deletion can ever belong to a different TaskList than
        // the currently displayed TaskList in this fragment.. Even if the user is editing a Task and
        // selects different TaskLists (without committing the changes), that Task still belongs to the
        // current TaskList displayed UNTIL he hits save. As such, I think the overloaded method that
        // simply takes the position argument suffices.

        /*int taskPos = task.indexInList();
        int listIndex = task.getTaskListIndex();
        TaskListDb.getList(listIndex).deleteTask(taskPos);
        if (mTaskList.getIndex() == listIndex) {
            mTaskAdapter.notifyItemRemoved(taskPos);
        }*/
    }

    public void notifyTaskDeleted(int at) {
        /*if (isShowingCompletedTasks()) {
            if (mTaskList instanceof CompletedTaskList) {
                mTaskList.deleteTask(at);
            } else {
                // This casting will succeed since the first if-check passed.
                ((CustomTaskList)mTaskList).deleteCompletedTask(at);
            }
        } else {
            if (mTaskList instanceof PrimaryTaskList) {
                // Have the actual TaskList delete the task, which in turn will delete the task
                // from the primaries too
                Task task = mTaskList.getTask(at);
                CustomTaskList actual = (CustomTaskList) TaskListDb.getList(task.getTaskListIndex());
                actual.deleteTask(task);
            } else {
                // Is an instanceof CustomTaskList
                mTaskList.deleteTask(at);
            }
        }
        // Regardless of which TaskList is currently displayed, it will need to be notified that
        // the item at position `at` is removed.
        mTaskAdapter.notifyItemRemoved(at);*/
    }

    public void notifyTaskUpdated(Task task) {
        /*Log.i("TaskListFrag", "notifyTaskUpdated()");
        // Check if the TaskListAdapter has an item entry at *layout* position specified by the
        // task's task list index. If not, a newly created TaskList has not been updated to the layout yet.
        if (mDrawerRecyclerView.findViewHolderForLayoutPosition(task.getTaskListIndex()) == null) {
            Log.i("TaskListFrag", "Notifying TaskListAdapter of new TaskList added");
            // A new TaskList was created for the task
            mTaskListAdapter.notifyItemInserted(task.getTaskListIndex());
        }*/
        // When editing a task, you can only select an instance of CustomTaskList, so this casting
        // will always succeed.
        /*CustomTaskList selectedList = (CustomTaskList) TaskListDb.getList(task.getTaskListIndex());
        if (selectedList != mTaskList) {
            if (mTaskList instanceof PrimaryTaskList) {
                Log.i("notifyTaskUpdated", "mTaskList instanceof PrimaryTaskList. Actual task list updating task.");
                selectedList.replaceTask(task.indexInList(), task);
                mTaskAdapter.notifyItemChanged(task.indexInList());
                return;
            }
            // First, check if the Task edited was a completed task
            if (isShowingCompletedTasks() && task.isCompleted()) {
                Log.i("TaskListFrag", "Updating a completed task, whose TaskList is not the current instance");
                ((CustomTaskList)mTaskList).deleteCompletedTask(task.indexInList());
                mTaskAdapter.notifyItemRemoved(task.indexInList());
                selectedList.addCompletedTask(task);
                return;
            } else {
                // The Task has changed TaskLists, so move from its original list to the selected list
                mTaskList.deleteTask(task.indexInList());
                mTaskAdapter.notifyItemRemoved(task.indexInList());
                // If this task is completed, then this method will instead put it in the completed list
                selectedList.addTask(task); // This method also sets the Task's task list index field
                return;
            }
        }

        // Selected list == current instance of TaskList, but is a completed Task
        if (isShowingCompletedTasks() && task.isCompleted()) {
            Log.i("TaskListFrag", "Updating a completed task");
            selectedList.replaceCompletedTask(task.indexInList(), task);
            mTaskAdapter.notifyItemChanged(task.indexInList());
            return;
        }

        // The Task has not changed its original TaskList
        int oldPos = task.indexInList();
        int newPos = selectedList.replaceTask(oldPos, task);
        if (oldPos != newPos) {
            mTaskAdapter.notifyItemMoved(oldPos, newPos);
        }
        mTaskAdapter.notifyItemChanged(newPos);*/
    }

    public int indexOfCurrentTaskList() {
        return mTaskList.getIndex();
    }

    public void reactToQuickAdd(boolean isAdded) {
        if (isAdded) {
            mFab.setVisibility(View.GONE);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START);
        } else {
            mFab.setVisibility(View.VISIBLE);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START);
        }
    }

    public void relayTaskListAdded(int at) {
        //mTaskListAdapter.notifyItemInserted(at);
        // Go to this TaskList
        mTaskList = TaskListDb.getList(at);
        mTitle.setText(mTaskList.getName());
        mTaskAdapter.changeTaskList(mTaskList);
        //mTaskAdapter.toggleTasks();
        // Close drawer
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    public void relayTaskListUpdated(int at) {
        mTaskListAdapter.notifyItemChanged(mTaskList.getIndex());
        mTaskAdapter.notifyDataSetChanged(); // Update each Task's TaskList string (in the Viewholder).
        // Always update the Toolbar's title, since at this point we just returned from editing
        // the currently displayed TaskList. There can never be a case where you're editing a task list
        // that is NOT the current instance of TaskList in this fragment.
        //
        // And by that logic, I think you can ignore the 'at' parameter.. see first line of method of what I did.
        Log.i("TaskListFrag", "TaskList updated.");
        mTitle.setText(mTaskList.getName()); // The Toolbar's title is placed in the child TextView.
    }

    public void relayTaskListDeleted(int at) {
        mTaskListAdapter.notifyItemRemoved(at);
        // TODO: Undecided as to where to go when TaskList is deleted..
        try {
            // Get the TaskList that now occupies the position that we just deleted from
            // Or maybe leave blank and show "No task list selected"??
            // Or go to 'All'?
            mTaskList = TaskListDb.getList(at);
        } catch (IndexOutOfBoundsException e) {
            Log.i("TaskListFrag", "After TaskList deleted, failed to retrieve next instance of TaskList.");
            // TODO: Create a TaskList instance that cannot be deleted. Switch to this TaskList if
            // we catch the exception.
            // However, for now, we will just retrieve the first TaskList.
            Log.i("TaskListFrag", "First TaskList chosen instead.");
            mTaskList = TaskListDb.getList(0);
        }
        // No need to worry about toggling views
        mTaskAdapter.changeTaskList(mTaskList);
        mTaskAdapter.toggleTasks(false);
        mTitle.setText(mTaskList.getName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TaskListDb.unregisterTaskListAdapter(mTaskListAdapter);
        mTaskListAdapter = null;
    }

    private void selectSortMenuOption() {
        if (!mMenu.findItem(R.id.action_sort).isVisible())
            return;
        Comparator<Task> c = mTaskList.getComparator();
        if (c == ComparatorUtils.IMPORTANCE || c == ComparatorUtils.REV_IMPORTANCE) {
            mSortMenu.findItem(R.id.by_importance).setChecked(true);
        } else if (c == ComparatorUtils.NAME || c == ComparatorUtils.REV_NAME) {
            mSortMenu.findItem(R.id.by_name).setChecked(true);
        } else if (c == ComparatorUtils.DUE_DATE || c == ComparatorUtils.REV_DUE_DATE) {
            mSortMenu.findItem(R.id.by_due_date).setChecked(true);
        } else if (c == ComparatorUtils.ORDER_CREATED || c == ComparatorUtils.REV_ORDER_CREATED) {
            mSortMenu.findItem(R.id.by_order_created).setChecked(true);
        } else if (c == ComparatorUtils.MY_ORDER) {
            mSortMenu.findItem(R.id.my_order).setChecked(true);
        }
    }

    private boolean isShowingCompletedTasks() {
        return mTaskAdapter.isShowingCompletedTasks();
    }

    // Reconfigures views to match up with the state of the currently displayed tasks (i.e. complete and incomplete)
    private void toggleViewsBasedOnStatusOfTasks() {
        if (isShowingCompletedTasks()) {
            // Reconfigure to match up with incomplete tasks
            // TODO replace android.R.id.home button with hamburger icon
            mFab.show();
            manageMenuOptionsBasedOnStatusOfTasks(true); // reverse == true
            mTaskAdapter.toggleTasks(false);
            selectSortMenuOption();
        } else {
            // Reconfigure to match up with completed tasks
            // TODO replace android.R.id.home button with up icon... DONE
            mFab.hide();
            manageMenuOptionsBasedOnStatusOfTasks(true); // reverse == true
            mTaskAdapter.toggleTasks(true);
            // Can't sort completed tasks, so no need for selectSortMenuOption();
        }
    }

    private void manageMenuOptionsBasedOnStatusOfTasks(boolean reverse) {
        // The default logic, when reverse == false, is to match the menu options with
        // the current state of the displayed tasks. E.g. if completed tasks are currently displaying,
        // then show the options that are permitted only for completed tasks.
        //
        // The default logic is based on the "natural" boolean that WILL display the option.
        // E.g. sort is visible if !completedTasksAreDisplaying
        //      view completed tasks is visible if !completedTasksAreDisplaying
        //      edit task list is visible if !completedTasksAreDisplaying
        //      clear completed tasks is visible if completedTasksAreDisplaying
        //      go back is visible if completedTasksAreDisplaying
        //
        // The reverse logic, when reverse == true, is to act as a toggle. E.g. if completed tasks
        // are currently displaying, then show the options for the opposite state, which is
        // incompleted tasks.
        boolean b = reverse ? isShowingCompletedTasks() : !isShowingCompletedTasks();
        mMenu.findItem(R.id.action_sort).setVisible(b);
        mMenu.findItem(R.id.view_completed_tasks).setVisible(b);
        mMenu.findItem(R.id.edit_task_list).setVisible(b);
        mMenu.findItem(R.id.action_go_back).setVisible(!b);
        mMenu.findItem(R.id.clear_completed_tasks).setVisible(!b);
    }

    private void manageSupportedMenuOptions() {
        for (Map.Entry<TaskList.MenuOptions, Integer> entry : sMenuOptions.entrySet()) {
            mMenu.findItem(entry.getValue()).setVisible(mTaskList.isMenuOptionEnabled(entry.getKey()));
        }
    }

    private boolean onSortOptionSelected(MenuItem item) {
        item.setChecked(true);
        int id = item.getItemId();
        Comparator<Task> c = mTaskList.getComparator();
        
        if (id == R.id.by_due_date) {
            if (c == ComparatorUtils.DUE_DATE) {
                mTaskList.setComparator(ComparatorUtils.REV_DUE_DATE);
            } else {
                mTaskList.setComparator(ComparatorUtils.DUE_DATE);
            }
        } else if (id == R.id.by_importance) {
            if (c == ComparatorUtils.IMPORTANCE) {
                mTaskList.setComparator(ComparatorUtils.REV_IMPORTANCE);
            } else {
                mTaskList.setComparator(ComparatorUtils.IMPORTANCE);
            }
        } else if (id == R.id.by_name) {
            if (c == ComparatorUtils.NAME) {
                mTaskList.setComparator(ComparatorUtils.REV_NAME);
            } else {
                mTaskList.setComparator(ComparatorUtils.NAME);
            }
        } else if (id == R.id.by_order_created) {
            if (c == ComparatorUtils.ORDER_CREATED) {
                mTaskList.setComparator(ComparatorUtils.REV_ORDER_CREATED);
            } else {
                mTaskList.setComparator(ComparatorUtils.ORDER_CREATED);
            }
        } else if (id == R.id.my_order) {
            mTaskList.setComparator(ComparatorUtils.MY_ORDER);
        }
        mTaskAdapter.notifyDataSetChanged();
        return true;
    }

}
