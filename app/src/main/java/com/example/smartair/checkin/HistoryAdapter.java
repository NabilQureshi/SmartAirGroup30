package com.example.smartair.checkin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HistoryEntry> items;

    public HistoryAdapter(List<HistoryEntry> items) {
        this.items = items;
    }

    public void update(List<HistoryEntry> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {

        TextView tvDate, tvSummary, tvTriggers, tvSubmittedBy;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.h_date);
            tvSummary = itemView.findViewById(R.id.h_summary);
            tvTriggers = itemView.findViewById(R.id.h_triggers);
            tvSubmittedBy = itemView.findViewById(R.id.h_by);
        }
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_entry, parent, false);
        return new HistoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryEntry e = items.get(position);
        holder.tvDate.setText(e.date);
        holder.tvSummary.setText(e.summary);
        holder.tvTriggers.setText("Triggers: " + e.triggers);
        holder.tvSubmittedBy.setText("Submitted by: " + e.submittedBy);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }
}
