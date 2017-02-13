package com.gmail.philliphsudev.progress.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.philliphsudev.progress.CalendarUtils;
import com.gmail.philliphsudev.progress.R;
import com.gmail.philliphsudev.progress.activities.EditTaskActivity;
import com.gmail.philliphsudev.progress.model.History;

import java.util.HashMap;

public class DetailedHistoryFragment extends android.support.v4.app.Fragment {
    private static final String ARG_HISTORY = "history";
    public static final String EXTRA_HISTORY = "com.gmail.philliphsudev.progress.fragments.HISTORY";
    public static final String EXTRA_HAS_CHANGES = "com.gmail.philliphsudev.progress.fragments.HAS_CHANGES";

    private History mHistory;

    private CollapsingToolbarLayout mCollapsingToolbar;
    private Toolbar mToolbar;
    private TextView mTag, mDuration, mActualDuration;
    private RecyclerView mRecyclerView;
    private DetailedHistoryAdapter mDetailedHistoryAdapter;

    private static boolean mHasChanges = false;

    private HistoryRelayer mHistoryRelayer;
    public interface HistoryRelayer {
        void relayHistory(History h);
    }

    public static DetailedHistoryFragment newInstance(History history) {
        if (history == null)
            throw new IllegalArgumentException("Cannot make instance of DetailedHistoryFragment with history==null");
        DetailedHistoryFragment fragment = new DetailedHistoryFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_HISTORY, history);
        fragment.setArguments(args);
        return fragment;
    }

    public DetailedHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // TODO This fragment is currently also used by DetailedHistoryActivity, which is outdated; it does not
        // implement the HistoryRelayer interface so this check is just a workaround so the compiler doesn't complain.
        if (activity instanceof EditTaskActivity) {
            try {
                mHistoryRelayer = (HistoryRelayer) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement HistoryRelayer");
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mHistory = getArguments().getParcelable(ARG_HISTORY);
        }
        mDetailedHistoryAdapter = new DetailedHistoryAdapter(mHistory);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detailed_history, container, false);
        mCollapsingToolbar = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        mTag = (TextView) view.findViewById(R.id.tag);
        mDuration = (TextView) view.findViewById(R.id.duration);
        mActualDuration = (TextView) view.findViewById(R.id.actual_duration);
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        //((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        //((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.inflateMenu(R.menu.menu_detailed_history);
        mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHasChanges) {
                    mHistoryRelayer.relayHistory(mHistory);
                }
                getFragmentManager().popBackStack();
            }
        });
        //((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(mHistory.getTag());
        mCollapsingToolbar.setTitle("History description for a task"); // Transparent when toolbar is expanded; visible only when collapsed
        mTag.setText("History description for a task");
        mDuration.setText(CalendarUtils.getTimeRangeString(mHistory.firstEventTime(), mHistory.lastEventTime()));

        mRecyclerView.setAdapter(mDetailedHistoryAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_detailed_history, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.i("DetailedHistory", "Has changes? " + mHasChanges);
                if (!mHasChanges) {
                    Log.i("DetailedHistory", "Setting RESULT_CANCELED");
                    getActivity().setResult(Activity.RESULT_CANCELED);
                } else {
                    Intent data = new Intent();
                    data.putExtra(EXTRA_HAS_CHANGES, mHasChanges);
                    data.putExtra(EXTRA_HISTORY, mHistory);
                    getActivity().setResult(Activity.RESULT_OK, data);
                }
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHasChanges = false;
    }

    private static final class DetailedHistoryAdapter extends RecyclerView.Adapter<DetailedHistoryAdapter.ViewHolder> {

        // Tracks the original comments at each position in the adapter.
        private HashMap<Integer, String> mOriginalComments = new HashMap<>();

        private History mHistory;

        public static final class ViewHolder extends RecyclerView.ViewHolder {

            private TextView time;
            private EditText comments;
            private ImageView rating;

            public ViewHolder(View itemView) {
                super(itemView);
                time = (TextView) itemView.findViewById(R.id.time);
                comments = (EditText) itemView.findViewById(R.id.comments);
                rating = (ImageView) itemView.findViewById(R.id.rating);
            }
        }

        public DetailedHistoryAdapter(History history) {
            mHistory = history;
        }

        @Override
        public DetailedHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_event, parent, false);
            final ViewHolder vh = new ViewHolder(view);

            vh.comments.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    vh.comments.setBackgroundResource(hasFocus ?
                            R.drawable.abc_edit_text_material : android.R.color.transparent);
                }
            });

            return vh;
        }

        @Override
        public void onBindViewHolder(final DetailedHistoryAdapter.ViewHolder holder, final int position) {
            holder.time.setText(CalendarUtils.getTimeString(mHistory.getEventTime(position)));
            holder.comments.setText(mHistory.getEventComments(position));

            if (!mOriginalComments.containsKey(position)) {
                mOriginalComments.put(position, mHistory.getEventComments(position));
                holder.comments.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        Log.i("ViewHolder", "afterTextChanged() at pos. " + position);
                        if (!s.toString().equals(mOriginalComments.get(position))) {
                            Log.i("ViewHolder", "Changed text != original text");
                            mHasChanges = true;
                            mHistory.getEventAt(holder.getAdapterPosition()).setComments(s.toString());
                        }
                    }
                });
            }
            // TODO holder.rating;
        }

        @Override
        public int getItemCount() {
            return mHistory.getEvents().size();
        }

    }
}
