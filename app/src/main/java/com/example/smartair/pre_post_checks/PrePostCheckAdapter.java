package com.example.smartair.pre_post_checks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrePostCheckAdapter extends RecyclerView.Adapter<PrePostCheckAdapter.CheckViewHolder> {

    private List<DocumentSnapshot> checkList = new ArrayList<>();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public void setChecks(List<DocumentSnapshot> checks) {
        this.checkList = checks != null ? checks : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addCheck(DocumentSnapshot check) {
        checkList.add(0, check); // 新记录插入顶部
        notifyItemInserted(0);
    }

    @NonNull
    @Override
    public CheckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pre_post_check, parent, false);
        return new CheckViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckViewHolder holder, int position) {
        holder.bind(checkList.get(position));
    }

    @Override
    public int getItemCount() {
        return checkList.size();
    }

    static class CheckViewHolder extends RecyclerView.ViewHolder {

        TextView tvWhenResult, tvRating, tvNote, tvTimestamp;

        public CheckViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWhenResult = itemView.findViewById(R.id.tvWhenResult);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvNote = itemView.findViewById(R.id.tvNote);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }

        public void bind(DocumentSnapshot doc) {
            String when = doc.getString("when");
            String result = doc.getString("result");
            Double rating = doc.getDouble("rating");
            String note = doc.getString("note");
            Long ts = doc.getLong("timestamp");

            tvWhenResult.setText((when != null ? when : "-") + " | " + (result != null ? result : "-"));
            tvRating.setText("Rating: " + (rating != null ? rating : "-"));

            if (note != null && !note.isEmpty()) {
                tvNote.setText("Note: " + note);
                tvNote.setVisibility(View.VISIBLE);
            } else {
                tvNote.setVisibility(View.GONE);
            }

            if (ts != null) {
                tvTimestamp.setText(sdf.format(new Date(ts)));
            } else {
                tvTimestamp.setText("-");
            }
        }
    }
}
