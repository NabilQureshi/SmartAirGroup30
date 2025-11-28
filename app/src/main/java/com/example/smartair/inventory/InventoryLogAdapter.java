package com.example.smartair.inventory;

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

public class InventoryLogAdapter extends RecyclerView.Adapter<InventoryLogAdapter.LogViewHolder> {

    private List<DocumentSnapshot> inventoryList = new ArrayList<>();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public void setInventory(List<DocumentSnapshot> list) {
        this.inventoryList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        holder.bind(inventoryList.get(position));
    }

    @Override
    public int getItemCount() {
        return inventoryList.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView typeText, doseText, dateText;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            typeText = itemView.findViewById(R.id.type_text);
            doseText = itemView.findViewById(R.id.dose_text);
            dateText = itemView.findViewById(R.id.date_text);
        }

        public void bind(DocumentSnapshot doc) {
            String type = doc.getString("type");
            Long dose = doc.getLong("dose");
            String date = doc.getString("date");

            typeText.setText(type != null ? type : "-");
            doseText.setText("Dose: " + (dose != null ? dose : "-") + " puffs");

            if (type != null) {
                if (type.equalsIgnoreCase("Rescue")) {
                    typeText.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_orange_dark));
                } else if (type.equalsIgnoreCase("Controller")) {
                    typeText.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark));
                }
            }

            dateText.setText(date != null ? date : "-");
        }
    }
}
