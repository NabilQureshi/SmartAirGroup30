package com.example.smartair.medicine_logs;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MedicineLogAdapter extends RecyclerView.Adapter<MedicineLogAdapter.LogViewHolder> {

    private List<DocumentSnapshot> logList = new ArrayList<>();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private boolean showSharedTag;

    public void setShowSharedTag(boolean showSharedTag) {
        this.showSharedTag = showSharedTag;
        notifyDataSetChanged();
    }

    public void setLogs(List<DocumentSnapshot> logs) {
        this.logList = logs != null ? logs : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        holder.bind(logList.get(position), showSharedTag);
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {

        TextView typeText, doseText, timestampText, sharedTag;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            typeText = itemView.findViewById(R.id.type_text);
            doseText = itemView.findViewById(R.id.dose_text);
            timestampText = itemView.findViewById(R.id.timestamp_text);
            sharedTag = itemView.findViewById(R.id.shared_tag);
        }

        public void bind(DocumentSnapshot doc, boolean showSharedTag) {
            String type = doc.getString("type");
            Long dose = doc.getLong("dose");
            Long ts = doc.getLong("timestamp");

            typeText.setText(type != null ? type : "-");
            doseText.setText("Dose: " + (dose != null ? dose : "-") + " puffs");

            if (type != null) {
                if (type.equalsIgnoreCase("Rescue")) {
                    typeText.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_orange_dark));
                } else if (type.equalsIgnoreCase("Controller")) {
                    typeText.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark));
                }
            }

            if (ts != null) {
                timestampText.setText(sdf.format(new Date(ts)));
            } else {
                timestampText.setText("-");
            }

            sharedTag.setVisibility(showSharedTag ? View.VISIBLE : View.GONE);
        }
    }
}
