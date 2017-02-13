package com.gmail.philliphsudev.progress.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.gmail.philliphsudev.progress.fragments.ActionItemsFragment;
import com.gmail.philliphsudev.progress.fragments.EditActionItemFragment;
import com.gmail.philliphsudev.progress.model.ActionItem;

import java.util.List;

public class ActionItemsActivity extends AppCompatActivity implements ActionItemsFragment.Starter {
    public static final String EXTRA_ACTION_ITEM = "com.gmail.philliphsudev.progress.activities.ACTION_ITEM";
    public static final int REQUEST_SHOW_ACTION_ITEM = 0;
    public static final int REQUEST_CREATE_ACTION_ITEM = 1;

    private ActionItemsFragment mActionItemsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<ActionItem> actionItems = getIntent()
                .getParcelableArrayListExtra(TaskActivity.EXTRA_ACTION_ITEMS);
        if (actionItems == null) {
            throw new IllegalArgumentException("Cannot start ActionItemsActivity with actionItems == null");
        }
        String taskName = getIntent().getStringExtra(TaskActivity.EXTRA_TASK_NAME);

        mActionItemsFragment = ActionItemsFragment.newInstance(taskName, actionItems);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, mActionItemsFragment).commit();
        }
    }

    @Override
    public void startEditActionItemActivity(ActionItem item, int requestCode) {
        Intent intent = new Intent(this, EditActionItemActivity.class);
        intent.putExtra(EXTRA_ACTION_ITEM, item);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED)
            return;

        if (requestCode == REQUEST_SHOW_ACTION_ITEM) {
            boolean actionItemChanged = data.getBooleanExtra(EditActionItemFragment.EXTRA_CHANGES_MADE, false);
            if (!actionItemChanged) {
                Log.i("ActionItemsActivity", "Action item was unchanged");
                return;
            }

            ActionItem item = data.getParcelableExtra(EditActionItemFragment.EXTRA_FINISHED_ACTION_ITEM);
            if (item == null) {
                throw new IllegalArgumentException("Finished ActionItem == null");
            }

            boolean delete = data.getBooleanExtra(EditActionItemFragment.EXTRA_DELETE_ACTION_ITEM, false);
            if (delete) {
                Log.i("ActionItemsActivity", "Action item to be deleted");
                mActionItemsFragment.notifyActionItemDeleted(item);
            } else {
                mActionItemsFragment.notifyActionItemUpdated(item);
            }
        } else if (requestCode == REQUEST_CREATE_ACTION_ITEM) {
            Log.i("ActionItemsActivity", "onActivityResult for request code REQUEST_CREATE_ACTION_ITEM");
            boolean hasChanges = data.getBooleanExtra(EditActionItemFragment.EXTRA_CHANGES_MADE, false);
            if (!hasChanges) {
                Log.i("ActionItemsActivity", "No values set for new Action Item; not creating");
                return;
            }
            ActionItem item = data.getParcelableExtra(EditActionItemFragment.EXTRA_FINISHED_ACTION_ITEM);
            if (item == null) {
                throw new IllegalArgumentException("Finished ActionItem == null");
            }
            mActionItemsFragment.notifyActionItemCreated(item);
        }
    }
}
