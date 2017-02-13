package com.gmail.philliphsudev.progress;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmail.philliphsudev.progress.model.WorkInterval;

import java.util.List;

/**
 * Created by Phillip Hsu on 8/29/2015.
 */
public class WorkIntervalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_ADD_BUTTON = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    private List<WorkInterval> mSchedule;

    public WorkIntervalAdapter(List<WorkInterval> schedule) {
        mSchedule = schedule;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_ADD_BUTTON:
                View addBtn = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.add_work_interval_button, parent, false);
                return new AddBtnViewHolder(addBtn);
            case VIEW_TYPE_ITEM:
            default:
                View itemView = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.item_work_interval, parent, false);
                return new ItemViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            WorkInterval wi = mSchedule.get(position);
            ItemViewHolder ivh = (ItemViewHolder) holder;
            ivh.desc.setText(wi.getDescription());
            ivh.range.setText(CalendarUtils.getDateTimeRangeString(wi.getStartDate(), wi.getEndDate()));
        }
        // Don't need to do anything if holder instanceof AddBtnVH
    }

    @Override
    public int getItemViewType(int position) {
        // I think we need to subtract 1...
        if (position == getItemCount() - 1) {
            // This is the position of the 'add' button
            return VIEW_TYPE_ADD_BUTTON;
        }
        // Every other position is an actual item, i.e. WorkInterval
        return VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return mSchedule.size() + 1; // Account for the 'add work interval button' that is always present at the end of the list
    }

    private static final class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView desc;
        private TextView range;

        public ItemViewHolder(View itemView) {
            super(itemView);
            desc = (TextView) itemView.findViewById(R.id.desc);
            range = (TextView) itemView.findViewById(R.id.range);
        }
    }

    private static final class AddBtnViewHolder extends RecyclerView.ViewHolder {

        public AddBtnViewHolder(View itemView) {
            super(itemView);
        }
    }
}
