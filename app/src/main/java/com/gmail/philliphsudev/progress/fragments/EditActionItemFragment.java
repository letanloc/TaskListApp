package com.gmail.philliphsudev.progress.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.gmail.philliphsudev.progress.CalendarUtils;
import com.gmail.philliphsudev.progress.DueDateTimeDialog;
import com.gmail.philliphsudev.progress.R;
import com.gmail.philliphsudev.progress.WorkIntervalView;
import com.gmail.philliphsudev.progress.model.ActionItem;
import com.gmail.philliphsudev.progress.model.WorkInterval;

import java.util.List;

public class EditActionItemFragment extends android.support.v4.app.Fragment {
    private static final String ARG_ACTION_ITEM = "com.gmail.philliphsudev.progress.fragments.ACTION_ITEM";

    public static final String EXTRA_FINISHED_ACTION_ITEM = "com.gmail.philliphsudev.progress.fragments.FINISHED_ACTION_ITEM";
    public static final String EXTRA_CHANGES_MADE = "com.gmail.philliphsudev.progress.fragments.CHANGES_MADE";
    public static final String EXTRA_DELETE_ACTION_ITEM = "com.gmail.philliphsudev.progress.fragments.DELETE_ACTION_ITEM";

    private Toolbar mToolbar;
    private EditText mName;
    private EditText mDateSelection;
    private FloatingActionButton mFab;
    private ActionItem mActionItem;
    private RecyclerView mRecyclerView;
    private boolean mChangesMade;
    private WorkIntervalAdapter mWorkIntervalAdapter;
    private MenuItem mSave;
    private boolean mIsCreating;

    private ActionMode.Callback NO_CONTEXT_MENU = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    };

    private TextWatcher mEditTextChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mChangesMade = true;
        }
    };

    public static EditActionItemFragment newInstance(ActionItem item) {
        if (item == null)
            throw new IllegalArgumentException("Cannot make instance of EditActionItemFragment with null ActionItem");
        EditActionItemFragment fragment = new EditActionItemFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ACTION_ITEM, item);
        fragment.setArguments(args);
        return fragment;
    }

    public EditActionItemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mActionItem = getArguments().getParcelable(ARG_ACTION_ITEM);
        }
        if (mActionItem.getLastPosition() < 0) {
            mIsCreating = true;
        }
        if (mActionItem.getWorkIntervals() != null) {
            Log.i("onCreate", "Num WorkIntervals: " + mActionItem.numWorkIntervals());
            mWorkIntervalAdapter = new WorkIntervalAdapter(
                    mActionItem.getWorkIntervals(),
                    new WorkIntervalAdapter.WorkIntervalViewActionRelayer() {
                        @Override
                        public void relayWorkIntervalViewSaved(WorkInterval interval) {
                            Log.i("Save", "relayWorkIntervalViewSaved()");
                            int oldPos = interval.getLastPosition();
                            int newPos = mActionItem.replaceWorkInterval(oldPos, interval);
                            if (oldPos != newPos) {
                                mWorkIntervalAdapter.notifyItemMoved(oldPos, newPos);
                            }
                            interval.setLastPosition(-1);
                            mWorkIntervalAdapter.notifyItemChanged(newPos);
                            mChangesMade = true;
                            mFab.show();
                        }

                        @Override
                        public void relayWorkIntervalViewDeleted(WorkInterval interval) {
                            Log.i("Delete", "relayWorkIntervalViewDeleted()");
                            mActionItem.deleteWorkInterval(interval.getLastPosition());
                            mWorkIntervalAdapter.notifyItemRemoved(interval.getLastPosition());
                            mFab.show();
                            mChangesMade = true;
                        }

                        /*@Override
                        public void relayWorkIntervalViewInvalidDuration() {
                            int at = mActionItem.numWorkIntervals() - 1;
                            Log.i("Relayer", "Recycled EVH at pos. " + at + " has invalid duration. Deleting.");
                            mActionItem.deleteWorkInterval(at);
                            mWorkIntervalAdapter.notifyItemRemoved(at);
                        }*/

                        @Override
                        public void relayExpandIntention(int at, WorkIntervalAdapter.CollapsedViewHolder collapsedVh) {
                            Log.i("Expand", "relayExpandIntention()");
                            int hasExpandedAt = mActionItem.indexOfExpandedWorkInterval();
                            if (hasExpandedAt >= 0) {
                                // A WorkInterval is currently expanded
                                // Remove the "is-expanded" marker/indication from this WorkInterval
                                WorkInterval wi = mActionItem.getWorkInterval(hasExpandedAt);
                                wi.setLastPosition(-1);
                                if (wi.getStartDate() == wi.getEndDate()) {
                                    // User tried to circumvent the save block by clicking away from the
                                    // zero-duration WorkInterval in mid-creation
                                    Log.i("Expand", "Caught the user trying to circumvent the save block!");
                                    mActionItem.deleteWorkInterval(hasExpandedAt);
                                    mWorkIntervalAdapter.notifyItemRemoved(hasExpandedAt);
                                } else {
                                    mWorkIntervalAdapter.notifyItemChanged(hasExpandedAt);
                                }
                            }

                            // Get the actual WorkInterval clicked at specified position, and attach the
                            // "is-expanded" marker onto this WorkInterval
                            mActionItem.getWorkInterval(at).setLastPosition(at);
                            mWorkIntervalAdapter.notifyItemChanged(at);
                            if (mFab.isShown()) mFab.hide();
                        }
                    }
            );
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_action_item, container, false);
        mToolbar          =     (Toolbar) view.findViewById(R.id.toolbar);
        mName             =     (EditText) view.findViewById(R.id.name);
        mDateSelection    =     (EditText) view.findViewById(R.id.date_selection);
        mFab              =     (FloatingActionButton) view.findViewById(R.id.fab);
        mRecyclerView     =     (RecyclerView) view.findViewById(R.id.recyclerView);
        
        mRecyclerView.setAdapter(mWorkIntervalAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        final AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(mToolbar);
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle(mActionItem.getLastPosition() >= 0 ?
                    R.string.edit_action_item : R.string.new_action_item);
        }

        mName.setText(mActionItem.getName());
        mDateSelection.setText(CalendarUtils.getDateTimeString(mActionItem.dueBy()));

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WorkInterval wi = new WorkInterval();
                int pos = mActionItem.addWorkIntervalAtEnd(wi);
                wi.setLastPosition(pos);
                mWorkIntervalAdapter.notifyItemInserted(pos);
                mFab.hide();
                mChangesMade = true;
            }
        });

        mName.addTextChangedListener(mEditTextChangeListener);
        mDateSelection.addTextChangedListener(mEditTextChangeListener);
        mDateSelection.setCustomSelectionActionModeCallback(NO_CONTEXT_MENU);
        mDateSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DueDateTimeDialog dialog = DueDateTimeDialog.newInstance(mActionItem.dueBy());
                dialog.show(getFragmentManager(), "DueDateTimeDialog");
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mName.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mName, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_edit_action_item, menu);
        mSave = menu.findItem(R.id.action_save);
        if (mIsCreating) {
            menu.findItem(R.id.action_delete).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                // Close any expanded views first
                final int hasExpandedAt = mActionItem.indexOfExpandedWorkInterval();
                if (hasExpandedAt >= 0) {
                    Log.i("SAVE", "There is an expanded WorkInterval.");
                    WorkIntervalAdapter.ExpandedViewHolder evh =
                            (WorkIntervalAdapter.ExpandedViewHolder)
                                    mRecyclerView.findViewHolderForAdapterPosition(hasExpandedAt);
                    if (!(evh.performSave())) {
                        // Try saving the expanded WorkInterval. If saving fails, then that means
                        // the conditions for enabling the WorkIntervalView's SAVE button were not met.
                        // I.e. this WorkInterval was probably default created, or was edited to have
                        // a zero duration.
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                        builder1.setTitle(R.string.title_zero_duration_work_interval_exists)
                                .setMessage(R.string.message_zero_duration_work_interval_exists)
                                .setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mActionItem.deleteWorkInterval(hasExpandedAt);
                                        mWorkIntervalAdapter.notifyItemRemoved(hasExpandedAt);
                                        mChangesMade = true;
                                        // Simulate hitting save again
                                        onOptionsItemSelected(mSave);
                                    }
                                })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                        builder1.show();
                        return true; // Consume the event, so as to stop code execution once the dialog is shown
                    } else {
                        Log.i("SAVE", "The expanded WorkInterval was closed and saved.");
                        mChangesMade = true;
                    }
                }
                mActionItem.setName(mName.getText().toString());
                Intent data = bundleActionItem(false);
                getActivity().setResult(Activity.RESULT_OK, data);
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
            case android.R.id.home:
                if (mChangesMade) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(R.string.discard_changes)
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    NavUtils.navigateUpFromSameTask(getActivity());
                                }
                            })
                            .setTitle(R.string.changes_not_saved);
                    builder.show();
                    return true; // Consume the event here, allowing us to override the behavior
                } else {
                    return false; // Allow normal behavior to proceed
                }
            case R.id.action_delete:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                builder2.setMessage(R.string.confirm_delete)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mChangesMade = true;
                                Intent data = bundleActionItem(true);
                                getActivity().setResult(Activity.RESULT_OK, data);
                                NavUtils.navigateUpFromSameTask(getActivity());
                            }
                        });
                builder2.show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onDueDateTimeSet(long dueDateTime) {
        mChangesMade = true;
        mDateSelection.setText(CalendarUtils.getDateTimeString(dueDateTime));
        mActionItem.setDueBy(dueDateTime);
    }

    private Intent bundleActionItem(boolean delete) {
        Intent data = new Intent();
        data.putExtra(EXTRA_CHANGES_MADE, mChangesMade);
        data.putExtra(EXTRA_FINISHED_ACTION_ITEM, mActionItem);
        data.putExtra(EXTRA_DELETE_ACTION_ITEM, delete);
        return data;
    }

    private static class WorkIntervalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int VIEW_TYPE_COLLAPSED = 0;
        private static final int VIEW_TYPE_EXPANDED = 1;

        private List<WorkInterval> mWorkIntervals;
        private static WorkIntervalViewActionRelayer mRelayer;

        // When the ExpandedViewHolder has consumed the event returned by the WorkIntervalView that it
        // encapsulates, use this interface to relay that signal back to the parent EditActionItemFragment class
        // so that it may facilitate this WorkIntervalAdapter's toggling of the appropriate ViewHolder.
        // (or deleting the item)
        public interface WorkIntervalViewActionRelayer {
            void relayWorkIntervalViewSaved(WorkInterval interval);
            void relayWorkIntervalViewDeleted(WorkInterval interval);

            // This method is functionally different from the above methods. Whereas the above methods
            // relay an action done on the WorkIntervalView, this method has nothing in relation to any WorkIntervalView;
            // instead, it only helps relays an intention to expand the current CollapsedViewHolder.
            // The expand() method found in the WorkIntervalAdapter's class
            // requires an instance of the adapter to be called, and the parent EditActionItemFragment class
            // happens to hold that required instance of the adapter.
            void relayExpandIntention(int at, CollapsedViewHolder collapsedVh);

            //void relayWorkIntervalViewInvalidDuration();
        }

        public static class ExpandedViewHolder extends RecyclerView.ViewHolder {

            private WorkIntervalView mView;

            public ExpandedViewHolder(WorkIntervalView itemView) {
                super(itemView);
                mView = itemView;

                // Listen to clicks on the SAVE and DELETE buttons within the WorkIntervalView
                // Once the event is consumed here, relay a signal to the parent EditActionItemFragment class
                // so that it may facilitate the WorkIntervalAdapter in toggling the appropriate ViewHolder
                // (or deleting the item)
                mView.setWorkIntervalViewActionListener(new WorkIntervalView.WorkIntervalViewActionListener() {
                    @Override
                    public void onWorkIntervalViewSave(WorkInterval interval) {
                        mRelayer.relayWorkIntervalViewSaved(interval);
                    }

                    @Override
                    public void onWorkIntervalViewDelete(WorkInterval interval) {
                        mRelayer.relayWorkIntervalViewDeleted(interval);
                    }
                });
            }

            public boolean performSave() {
                return mView.performSave();
            }
        }

        public static class CollapsedViewHolder extends RecyclerView.ViewHolder {

            private TextView mDateRange;
            private TextView mTimeRange;

            public CollapsedViewHolder(View itemView) {
                super(itemView);
                mDateRange = (TextView) itemView.findViewById(R.id.date_range);
                mTimeRange = (TextView) itemView.findViewById(R.id.time_range);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("CollapsedVH", "onClick");
                        // Get position of the item represented by this ViewHolder
                        mRelayer.relayExpandIntention(CollapsedViewHolder.this.getAdapterPosition(), CollapsedViewHolder.this);
                    }
                });
            }
        }

        public WorkIntervalAdapter(List<WorkInterval> intervals, WorkIntervalViewActionRelayer listener) {
            mWorkIntervals = intervals;
            mRelayer = listener;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case VIEW_TYPE_COLLAPSED:
                    View itemView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.work_interval_collapsed, parent, false);
                    return new CollapsedViewHolder(itemView);
                case VIEW_TYPE_EXPANDED:
                default:
                    return new ExpandedViewHolder(new WorkIntervalView(parent.getContext()));
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            WorkInterval interval = mWorkIntervals.get(position);
            if (holder instanceof ExpandedViewHolder) {
                Log.i("onBindViewHolder", "ViewHolder instanceof ExpandedViewHolder");
                ((ExpandedViewHolder)holder).mView.setWorkInterval(interval);
            } else if (holder instanceof CollapsedViewHolder) {
                Log.i("onBindViewHolder", "ViewHolder instanceof CollapsedViewHolder");
                ((CollapsedViewHolder)holder).mDateRange.setText(
                        CalendarUtils.getDateRangeString(interval.getStartDate(), interval.getEndDate()));
                ((CollapsedViewHolder)holder).mTimeRange.setText(
                        CalendarUtils.getTimeRangeString(interval.getStartDate(), interval.getEndDate()));
            } else {
                throw new IllegalArgumentException("WorkInterval ViewHolder type is not supported");
            }
        }

        @Override
        public int getItemCount() {
            return mWorkIntervals.size();
        }

        @Override
        public int getItemViewType(int position) {
            return mWorkIntervals.get(position).getLastPosition() >= 0 ?
                    VIEW_TYPE_EXPANDED : VIEW_TYPE_COLLAPSED;
        }

        @Override
        public void onViewRecycled(RecyclerView.ViewHolder holder) {
            Log.i("WorkIntervalAdapter", "onViewRecycled");
            super.onViewRecycled(holder);
            if (holder instanceof ExpandedViewHolder) {
                Log.i("WorkIntervalAdapter", "Recycled VH instanceof ExpandedViewHolder");
                ExpandedViewHolder evh = (ExpandedViewHolder) holder;
                evh.mView.clearEndOn();
                /*if (!evh.mView.hasValidDuration()) {
                    // User circumvented the save block, so delete the item, as last represented by
                    // the ExpandedViewHolder at its "old" position (i.e. "old" as in the evh was just recycled,
                    // as would be the case if this method is ran.)

                    if (!mWorkIntervals.isEmpty()) {
                        // Only do this deletion if user circumvented the save block by means of clicking
                        // away from the zero-duration WorkInterval mid-creation. If the user tries to hit
                        // save in the menu options, then allow that to handle its own deletion algorithm, and
                        // opt not to execute this if-block (since if the save menu item ends up deleting the WorkInterval,
                        // the list should be empty. )
                        mRelayer.relayWorkIntervalViewInvalidDuration();
                    }
                }*/
            }
        }

        // Turns out we didn't even need these!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        public void collapse(int at, ExpandedViewHolder expandedVh) {
            ViewGroup vg = (ViewGroup) expandedVh.itemView.getParent();
            RecyclerView.ViewHolder newVh = createViewHolder(vg, VIEW_TYPE_COLLAPSED);
            bindViewHolder(newVh, at);
            notifyItemChanged(at);
        }

        public void expand(int at, CollapsedViewHolder collapsedVh) {
            ViewGroup vg = (ViewGroup) collapsedVh.itemView.getParent();
            RecyclerView.ViewHolder newVh = createViewHolder(vg, VIEW_TYPE_EXPANDED);
            bindViewHolder(newVh, at);
            notifyItemChanged(at);
        }
    }

}
