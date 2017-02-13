package com.gmail.philliphsudev.progress.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.gmail.philliphsudev.progress.DueDateTimeDialog;
import com.gmail.philliphsudev.progress.fragments.EditActionItemFragment;
import com.gmail.philliphsudev.progress.model.ActionItem;

public class EditActionItemActivity extends AppCompatActivity implements DueDateTimeDialog.OnDateTimeSetListener {

    private EditActionItemFragment mEditActionItemFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionItem item = getIntent().getParcelableExtra(ActionItemsActivity.EXTRA_ACTION_ITEM);
        if (item == null)
            throw new IllegalArgumentException("Cannot make instance of EditActionItemFragment with null ActionItem");

        mEditActionItemFragment = EditActionItemFragment.newInstance(item);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, mEditActionItemFragment).commit();
        }
    }

    @Override
    public void onDueDateTimeSet(long dueDateTime) {
        mEditActionItemFragment.onDueDateTimeSet(dueDateTime);
    }
}
