package com.gmail.philliphsudev.progress.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.TextView;

import com.gmail.philliphsudev.progress.CalendarUtils;
import com.gmail.philliphsudev.progress.R;
import com.gmail.philliphsudev.progress.RecyclerItemClickListener;
import com.gmail.philliphsudev.progress.model.History;

import java.util.ArrayList;
import java.util.Collections;

public class HistoryFragment extends android.support.v4.app.Fragment {
    private static final String ARG_TASK_NAME = "taskName";
    private static final String ARG_HISTORIES = "history";

    public static final String EXTRA_FINISHED_HISTORIES = "com.gmail.philliphsudev.progress.fragments.FINISHED_HISTORIES";
    public static final String EXTRA_HAS_CHANGES = "com.gmail.philliphsudev.progress.fragments.HAS_CHANGES";

    private String mTaskName;
    private ArrayList<History> mHistories;

    private Toolbar mToolbar;
    private TextView mTitle;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;
    private HistoryAdapter mHistoryAdapter;
    private boolean mHasChanges;
    private SortOrder mSortOrder = SortOrder.DESCENDING; // Any instance of this Fragment will always start off with a descending sort order

    private enum SortOrder { ASCENDING, DESCENDING }

    private Starter mStarter; // Static because I'm trying out having the RecyclerItem click listener in the ViewHolder!

    public interface Starter {
        void startDetailedHistoryActivity(History history);
    }

    public static HistoryFragment newInstance(String taskName, ArrayList<History> histories) {
        if (histories == null) {
            throw new IllegalArgumentException("Cannot create instance of HistoryFragment with histories == null");
        }
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TASK_NAME, taskName);
        args.putParcelableArrayList(ARG_HISTORIES, histories);
        fragment.setArguments(args);
        return fragment;
    }

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mStarter = (Starter) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Starter");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mTaskName = getArguments().getString(ARG_TASK_NAME);
            mHistories = getArguments().getParcelableArrayList(ARG_HISTORIES);
        }
        mHistoryAdapter = new HistoryAdapter(mHistories);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.toolbar_list_fab_layout, container, false);
        mToolbar       =     (Toolbar) view.findViewById(R.id.toolbar);
        mTitle         =     (TextView) view.findViewById(R.id.title);
        mRecyclerView  =     (RecyclerView) view.findViewById(R.id.main_recyclerView);
        mFab           =     (FloatingActionButton) view.findViewById(R.id.fab);

        mTitle.setText("History for\n" + mTaskName);

        final AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(mToolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView.setAdapter(mHistoryAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        // TODO Try implementing the on click listener in the ViewHolder instead!
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(activity, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                History history = mHistories.get(position);
                history.setLastPosition(position);
                mStarter.startDetailedHistoryActivity(history);
            }
        }));

        mFab.setImageResource(android.R.drawable.ic_menu_search);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });

        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_history_v2, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!mHasChanges) {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                } else {
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_HAS_CHANGES, mHasChanges);
                    intent.putParcelableArrayListExtra(EXTRA_FINISHED_HISTORIES, mHistories);
                    getActivity().setResult(Activity.RESULT_OK, intent);
                }
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
            case R.id.action_sort:
                if (mSortOrder == SortOrder.DESCENDING) {
                    // I.e. currently sorted latest - earliest so reversing the order sorts the list
                    // earliest - latest
                    mSortOrder = SortOrder.ASCENDING; // Change the SortOrder
                    Collections.sort(mHistories, Collections.reverseOrder());
                } else if (mSortOrder == SortOrder.ASCENDING) {
                    // I.e. currently sorted earliest - latest, so sort using the natural ordering (i.e. Descending)
                    mSortOrder = SortOrder.DESCENDING;
                    Collections.sort(mHistories);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void notifyHistoryChanged(History history) {
        mHasChanges = true;
        mHistories.set(history.getLastPosition(), history);
        mHistoryAdapter.notifyItemChanged(history.getLastPosition());
        // Don't need to sort or worry about item movement since History time values cannot be modified..
    }

    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

        private ArrayList<History> mHistories; // Static because this needs to be accessible in the ViewHolder

        public static class ViewHolder extends RecyclerView.ViewHolder /*implements View.OnClickListener*/ {

            private TextView mDescription;
            private TextView mHappenedOn;

            public ViewHolder(View itemView) {
                super(itemView);
                mDescription = (TextView) itemView.findViewById(R.id.description);
                mHappenedOn = (TextView) itemView.findViewById(R.id.happened_on);
            }

            /*@Override
            public void onClick(View v) {
                Log.i("HistoryAdapter", "startDetailedHistoryActivity");
                mStarter.startDetailedHistoryActivity(mHistories.get(getAdapterPosition()));
            }*/
        }

        public HistoryAdapter(ArrayList<History> histories) {
            mHistories = histories;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_v2, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            History history = mHistories.get(position);
            String tag = history.getTag();
            holder.mDescription.setText(tag.isEmpty() ? "History description #" + position : tag); // TODO
            holder.mHappenedOn.setText(CalendarUtils.getDateTimeRangeString(
                    history.firstEventTime(), history.lastEventTime())
            );
        }

        @Override
        public int getItemCount() {
            return mHistories.size();
        }
    }
}
