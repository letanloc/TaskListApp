package com.gmail.philliphsudev.progress.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.gmail.philliphsudev.progress.fragments.DetailedHistoryFragment;
import com.gmail.philliphsudev.progress.model.History;

public class DetailedHistoryActivity extends AppCompatActivity {

    private DetailedHistoryFragment mDetailedHistoryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        History history = getIntent().getParcelableExtra(HistoryActivity.EXTRA_HISTORY);
        if (history == null) {
            throw new IllegalArgumentException("Cannot start DetailedHistoryActivity with history == null");
        }
        mDetailedHistoryFragment = DetailedHistoryFragment.newInstance(history);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, mDetailedHistoryFragment).commit();
        }
    }
}
