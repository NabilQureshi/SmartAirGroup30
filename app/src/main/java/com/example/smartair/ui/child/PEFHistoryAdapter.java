package com.example.smartair.ui.child;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.model.PEFEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying PEF entry history.
 * Shows PEF values with pre/post-medicine tags to help children see
 * how medicine affects breathing.
 */
public class PEFHistoryAdapter extends RecyclerView.Adapter<PEFHistoryAdapter.PEFViewHolder> {

    private List<PEFEntry> pefEntries;
    private final SimpleDateFormat dateFormat;
    private boolean showSharedTag;

    public PEFHistoryAdapter() {
        this.pefEntries = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
    }

    public void setPEFEntries(List<PEFEntry> entries) {
        this.pefEntries = entries != null ? entries : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setShowSharedTag(boolean showSharedTag) {
        this.showSharedTag = showSharedTag;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PEFViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pef_entry, parent, false);
        return new PEFViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PEFViewHolder holder, int position) {
        PEFEntry entry = pefEntries.get(position);
        holder.bind(entry, showSharedTag);
    }

    @Override
    public int getItemCount() {
        return pefEntries.size();
    }

    static class PEFViewHolder extends RecyclerView.ViewHolder {

        private final TextView pefValueText;
        private final TextView medicineTagText;
        private final TextView timestampText;
        private final TextView comparisonText;
        private final TextView sharedTagText;
        private final SimpleDateFormat dateFormat;

        public PEFViewHolder(@NonNull View itemView) {
            super(itemView);
            pefValueText = itemView.findViewById(R.id.pef_value_text);
            medicineTagText = itemView.findViewById(R.id.medicine_tag_text);
            timestampText = itemView.findViewById(R.id.timestamp_text);
            comparisonText = itemView.findViewById(R.id.comparison_text);
            sharedTagText = itemView.findViewById(R.id.shared_tag);
            this.dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        }

        public void bind(PEFEntry entry, boolean showSharedTag) {
            // 显示 PEF 数值
            pefValueText.setText(entry.getPefValue() + " L/min");

            // 显示 medicine 标签
            if (entry.getMedicineTag() != PEFEntry.MedicineTag.NONE) {
                medicineTagText.setText(entry.getMedicineTag().getDisplayName());
                medicineTagText.setVisibility(View.VISIBLE);

                // 根据标签颜色标记
                if (entry.isPreMedicine()) {
                    medicineTagText.setTextColor(
                            ContextCompat.getColor(itemView.getContext(), android.R.color.holo_orange_dark));
                } else if (entry.isPostMedicine()) {
                    medicineTagText.setTextColor(
                            ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark));
                }
            } else {
                medicineTagText.setVisibility(View.GONE);
            }

            // 显示时间戳
            Date date = new Date(entry.getTimestamp());
            timestampText.setText(dateFormat.format(date));

            // 显示对比信息
            if (entry.isPostMedicine()) {
                comparisonText.setVisibility(View.VISIBLE);
                comparisonText.setText("✓ After medicine");
                comparisonText.setTextColor(
                        ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark));
            } else if (entry.isPreMedicine()) {
                comparisonText.setVisibility(View.VISIBLE);
                comparisonText.setText("Before medicine");
                comparisonText.setTextColor(
                        ContextCompat.getColor(itemView.getContext(), android.R.color.holo_orange_dark));
            } else {
                comparisonText.setVisibility(View.GONE);
            }

            sharedTagText.setVisibility(showSharedTag ? View.VISIBLE : View.GONE);
        }
    }
}
