package com.example.smartair;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ChildViewHolder> {

    private List<Child> childList;
    private OnChildClickListener listener;

    // Interface for click handling
    public interface OnChildClickListener {
        void onChildClick(Child child, String childId);
    }

    public ChildAdapter(List<Child> childList, OnChildClickListener listener) {
        this.childList = childList;
        this.listener = listener;
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

        // Add the click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChildClick(child, child.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return childList.size();
    }

    public static class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView txtChildName, txtChildDOB, txtChildNotes;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            txtChildName = itemView.findViewById(R.id.txtChildName);
            txtChildDOB = itemView.findViewById(R.id.txtChildDOB);
            txtChildNotes = itemView.findViewById(R.id.txtChildNotes);
        }
    }
}
