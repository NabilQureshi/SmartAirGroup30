package com.example.smartair.child_managent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ChildViewHolder> {

    private List<Child> childList;
    private OnChildClickListener listener;
    private final FirebaseFirestore db;
    private final Map<String, ZoneDisplay> zoneCache = new HashMap<>();

    public interface OnChildClickListener {
        void onChildClick(Child child);
    }

    public ChildAdapter(List<Child> childList, OnChildClickListener listener) {
        this.childList = childList;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_child, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        Child child = childList.get(position);

        holder.txtChildName.setText(child.getName());
        holder.txtChildDOB.setText("DOB: " + child.getDob());
        holder.txtChildNotes.setText("Notes: " + child.getNotes());
        holder.txtChildUsername.setText("Username: " + child.getUsername());
        holder.txtZoneStatus.setText("Zone: Unknown");
        holder.txtZoneStatus.setBackgroundResource(R.color.zone_neutral_bg);
        holder.txtZoneStatus.setTextColor(holder.itemView.getResources().getColor(android.R.color.black));
        loadZoneForChild(child, holder);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChildClick(child);
            }
        });
    }

    @Override
    public int getItemCount() {
        return childList.size();
    }

    public static class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView txtChildName, txtChildDOB, txtChildNotes, txtChildUsername, txtZoneStatus;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            txtChildName = itemView.findViewById(R.id.txtChildName);
            txtChildDOB = itemView.findViewById(R.id.txtChildDOB);
            txtChildNotes = itemView.findViewById(R.id.txtChildNotes);
            txtChildUsername = itemView.findViewById(R.id.txtChildUsername);
            txtZoneStatus = itemView.findViewById(R.id.txtZoneStatus);
        }
    }

    private void loadZoneForChild(Child child, ChildViewHolder holder) {
        if (child == null || child.getUid() == null || child.getUid().isEmpty()) {
            applyZoneStyle(holder, null, null);
            return;
        }

        ZoneDisplay cached = zoneCache.get(child.getUid());
        if (cached != null) {
            applyZoneStyle(holder, cached.state, cached.percent);
            return;
        }

        final String uid = child.getUid();
        holder.txtZoneStatus.setTag(uid);

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!uid.equals(holder.txtZoneStatus.getTag())) return;
                    String state = doc.getString("latestZoneState");
                    Double percent = doc.getDouble("latestZonePercent");
                    zoneCache.put(uid, new ZoneDisplay(state, percent));
                    applyZoneStyle(holder, state, percent);
                })
                .addOnFailureListener(e -> {
                    if (!uid.equals(holder.txtZoneStatus.getTag())) return;
                    applyZoneStyle(holder, null, null);
                });
    }

    private void applyZoneStyle(ChildViewHolder holder, String state, Double percent) {
        String displayState = state != null ? state.toUpperCase() : "UNKNOWN";
        String percentText = percent != null ? String.format("%.0f%% PB", percent) : "";
        int bgRes;
        int textColorRes;

        switch (displayState) {
            case "GREEN":
                bgRes = R.color.zone_green_bg;
                textColorRes = R.color.zone_green_text;
                break;
            case "YELLOW":
                bgRes = R.color.zone_yellow_bg;
                textColorRes = R.color.zone_yellow_text;
                break;
            case "RED":
                bgRes = R.color.zone_red_bg;
                textColorRes = R.color.zone_red_text;
                break;
            default:
                bgRes = R.color.zone_neutral_bg;
                textColorRes = android.R.color.black;
                break;
        }

        String label = percentText.isEmpty()
                ? "Zone: " + displayState
                : "Zone: " + displayState + " (" + percentText + ")";

        holder.txtZoneStatus.setText(label);
        holder.txtZoneStatus.setBackgroundResource(bgRes);
        holder.txtZoneStatus.setTextColor(holder.itemView.getResources().getColor(textColorRes));
    }

    private static class ZoneDisplay {
        final String state;
        final Double percent;

        ZoneDisplay(String state, Double percent) {
            this.state = state;
            this.percent = percent;
        }
    }
}
