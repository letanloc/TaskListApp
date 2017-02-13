package com.gmail.philliphsudev.progress;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmail.philliphsudev.progress.model.History;

import java.util.List;

/**
 * Created by Phillip Hsu on 8/29/2015.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<History> mHistories;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mDescription;
        private TextView mHappenedOn;

        public ViewHolder(View itemView) {
            super(itemView);
            mDescription = (TextView) itemView.findViewById(R.id.description);
            mHappenedOn = (TextView) itemView.findViewById(R.id.happened_on);
        }
    }

    public HistoryAdapter(List<History> histories) {
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
                        history.firstEventTime(), history.lastEventTime()));
    }

    @Override
    public int getItemCount() {
        return mHistories.size();
    }
}
