package com.gmail.philliphsudev.progress.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.gmail.philliphsudev.progress.CalendarUtils;
import com.gmail.philliphsudev.progress.R;
import com.gmail.philliphsudev.progress.RecyclerItemClickListener;
import com.gmail.philliphsudev.progress.activities.ActionItemsActivity;
import com.gmail.philliphsudev.progress.model.ActionItem;
import com.gmail.philliphsudev.progress.model.WorkInterval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActionItemsFragment extends Fragment {
    private static final String ARG_ACTION_ITEMS = "com.gmail.philliphsudev.progress.fragments.ACTION_ITEMS";
    private static final String ARG_TASK_NAME = "com.gmail.philliphsudev.progress.fragments.TASK_NAME";

    public static final String EXTRA_HAS_CHANGES = "com.gmail.philliphsudev.progress.fragments.HAS_CHANGES";
    public static final String EXTRA_FINISHED_ACTION_ITEMS = "com.gmail.philliphsudev.progress.fragments.FINISHED_ACTION_ITEMS";

    private RecyclerView mRecyclerView;
    private Toolbar mToolbar;
    private TextView mTitle;
    private FloatingActionButton mFab;
    private ActionItemAdapter mActionItemAdapter;
    private List<ActionItem> mActionItems;
    private String mTaskName;
    private boolean mHasChanges;

    private Starter mStarter;

    public interface Starter {
        void startEditActionItemActivity(ActionItem item, int requestCode);
    }

    public static ActionItemsFragment newInstance(String taskName, List<ActionItem> actionItems) {
        ActionItemsFragment fragment = new ActionItemsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TASK_NAME, taskName);
        args.putParcelableArrayList(ARG_ACTION_ITEMS, (ArrayList<ActionItem>) actionItems);
        fragment.setArguments(args);
        return fragment;
    }

    public ActionItemsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mActionItems = getArguments().getParcelableArrayList(ARG_ACTION_ITEMS);
            mTaskName = getArguments().getString(ARG_TASK_NAME);
        }
        mActionItemAdapter = new ActionItemAdapter(mActionItems);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.toolbar_list_fab_layout, container, false);
        mToolbar          =    (Toolbar) view.findViewById(R.id.toolbar);
        mTitle            =    (TextView) view.findViewById(R.id.title);
        mRecyclerView     =    (RecyclerView) view.findViewById(R.id.main_recyclerView);
        mFab              =    (FloatingActionButton) view.findViewById(R.id.fab);

        mTitle.setText("Action items for\n" + mTaskName);
        final AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(mToolbar);
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mFab.setImageResource(android.R.drawable.ic_input_add);

        mRecyclerView.setAdapter(mActionItemAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(activity,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        ActionItem item = mActionItems.get(position);
                        item.setLastPosition(position);
                        mStarter.startEditActionItemActivity(item, ActionItemsActivity.REQUEST_SHOW_ACTION_ITEM);
                    }
                }));

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStarter.startEditActionItemActivity(new ActionItem(), ActionItemsActivity.REQUEST_CREATE_ACTION_ITEM);
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_action_items, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent();
                intent.putExtra(EXTRA_HAS_CHANGES, mHasChanges);
                intent.putParcelableArrayListExtra(
                        EXTRA_FINISHED_ACTION_ITEMS,
                        (ArrayList<ActionItem>) mActionItems);
                getActivity().setResult(Activity.RESULT_OK, intent);
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mStarter = (Starter) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Starter");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mStarter = null;
    }

    public void notifyActionItemCreated(ActionItem item) {
        mHasChanges = true;
        mActionItems.add(item);
        Collections.sort(mActionItems);
        mActionItemAdapter.notifyItemInserted(mActionItems.indexOf(item));
    }

    public void notifyActionItemUpdated(ActionItem item) {
        mHasChanges = true;
        int initialPos = item.getLastPosition();
        mActionItems.set(initialPos, item);
        mActionItemAdapter.notifyItemChanged(initialPos);
        Collections.sort(mActionItems);
        int finalPos = mActionItems.indexOf(item);
        if (initialPos != finalPos) {
            mActionItemAdapter.notifyItemMoved(initialPos, finalPos);
        }
    }

    public void notifyActionItemDeleted(ActionItem item) {
        mHasChanges = true;
        mActionItems.remove(item.getLastPosition());
        mActionItemAdapter.notifyItemRemoved(item.getLastPosition());
    }

    private static class ActionItemAdapter extends RecyclerView.Adapter<ActionItemAdapter.ViewHolder> {

        private List<ActionItem> actionItems;

        public static class ViewHolder extends RecyclerView.ViewHolder {

            private TextView mName;
            private TextView mDueBy;
            private TextView mUpcomingWorkInterval;
            private ImageButton mTimer;

            public ViewHolder(View itemView) {
                super(itemView);
                mName = (TextView) itemView.findViewById(R.id.name);
                mDueBy = (TextView) itemView.findViewById(R.id.due_date);
                mUpcomingWorkInterval = (TextView) itemView.findViewById(R.id.work_interval);
                mTimer = (ImageButton) itemView.findViewById(R.id.start_btn);
            }
        }

        public ActionItemAdapter(List<ActionItem> actionItems) {
            this.actionItems = actionItems;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.action_item, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ActionItem item = actionItems.get(position);
            holder.mName.setText(item.getName());
            holder.mDueBy.setText(CalendarUtils.getDateTimeString(item.dueBy()));
            holder.mDueBy.setVisibility(holder.mDueBy.length() > 0 ? View.VISIBLE : View.GONE);
            WorkInterval interval = item.upcomingWorkInterval();
            if (interval != null) {
                holder.mUpcomingWorkInterval.setText(CalendarUtils.getDateTimeRangeString(
                        interval.getStartDate(),
                        interval.getEndDate()
                ));
                holder.mUpcomingWorkInterval.setVisibility(
                        holder.mUpcomingWorkInterval.length() > 0 ? View.VISIBLE : View.GONE);
            } else {
                // Only show timer if ActionItem has no scheduled work interval
                holder.mTimer.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return actionItems.size();
        }
    }
}
