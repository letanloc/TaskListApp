package com.gmail.philliphsudev.progress.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.gmail.philliphsudev.progress.fragments.DetailedHistoryFragment;
import com.gmail.philliphsudev.progress.fragments.HistoryFragment;
import com.gmail.philliphsudev.progress.model.History;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity implements HistoryFragment.Starter {
    public static final String EXTRA_HISTORY = "com.gmail.philliphsudev.progress.activities.HISTORY";
    public static final int REQUEST_SHOW_HISTORY = 0;

    private HistoryFragment mHistoryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<History> histories = getIntent().getParcelableArrayListExtra(TaskActivity.EXTRA_HISTORY);
        if (histories == null) {
            throw new IllegalArgumentException("Cannot start HistoryActivity with histories == null");
        }
        String taskName = getIntent().getStringExtra(TaskActivity.EXTRA_TASK_NAME);

        mHistoryFragment = HistoryFragment.newInstance(taskName, histories);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, mHistoryFragment).commit();
        }
    }

    @Override
    public void startDetailedHistoryActivity(History history) {
        Intent intent = new Intent(this, DetailedHistoryActivity.class);
        intent.putExtra(EXTRA_HISTORY, history);
        startActivityForResult(intent, REQUEST_SHOW_HISTORY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED)
            return;
        if (requestCode == REQUEST_SHOW_HISTORY) {
            boolean historyChanged = data.getBooleanExtra(DetailedHistoryFragment.EXTRA_HAS_CHANGES, false);
            if (!historyChanged) {
                Log.i("HistoryActivity", "History unchanged");
                return;
            }

            History history = data.getParcelableExtra(DetailedHistoryFragment.EXTRA_HISTORY);
            if (history == null) {
                throw new NullPointerException("Finished History == null");
            }

            mHistoryFragment.notifyHistoryChanged(history);
        }
    }
}
