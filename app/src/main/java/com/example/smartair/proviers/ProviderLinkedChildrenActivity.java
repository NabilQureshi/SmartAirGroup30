package com.example.smartair.proviers;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProviderLinkedChildrenActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar loading;
    private TextView emptyText;
    private LinkedChildAdapter adapter;
    private final List<DocumentSnapshot> items = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_linked_children);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerLinkedChildren);
        loading = findViewById(R.id.loading);
        emptyText = findViewById(R.id.emptyText);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LinkedChildAdapter(items, this::launchDashboardWith, db);
        recyclerView.setAdapter(adapter);

        loadLinkedChildren();
    }

    private void loadLinkedChildren() {
        String providerId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (providerId == null) {
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loading.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance()
                .collection("providers")
                .document(providerId)
                .collection("linkedChildren")
                .get()
                .addOnSuccessListener(snapshot -> {
                    loading.setVisibility(View.GONE);
                    items.clear();
                    if (snapshot != null) {
                        items.addAll(snapshot.getDocuments());
                    }
                    adapter.notifyDataSetChanged();

                    if (items.isEmpty()) {
                        emptyText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    loading.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load linked children", Toast.LENGTH_SHORT).show();
                    emptyText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                });
    }

    private void launchDashboardWith(DocumentSnapshot doc) {
        Intent intent = new Intent(this, com.example.smartair.dashboard.DashboardProvidersActivity.class);
        intent.putExtra("childId", doc.getId());
        intent.putExtra("name", doc.getString("name"));
        intent.putExtra("username", doc.getString("username"));
        intent.putExtra("password", doc.getString("password"));
        intent.putExtra("dob", doc.getString("dob"));
        intent.putExtra("notes", doc.getString("notes"));
        startActivity(intent);
    }

    private static class LinkedChildAdapter extends RecyclerView.Adapter<LinkedChildAdapter.ViewHolder> {
        interface OnChildClick {
            void onClick(DocumentSnapshot doc);
        }

        private final List<DocumentSnapshot> data;
        private final OnChildClick callback;
        private final FirebaseFirestore db;
        private final java.util.Map<String, String> nameCache = new java.util.HashMap<>();

        LinkedChildAdapter(List<DocumentSnapshot> data, OnChildClick callback, FirebaseFirestore db) {
            this.data = data;
            this.callback = callback;
            this.db = db;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_child_simple, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DocumentSnapshot doc = data.get(position);
            String name = doc.getString("name");
            String username = doc.getString("username");
            String childId = doc.getId();

            if (nameCache.containsKey(childId)) {
                name = nameCache.get(childId);
            }

            if (name == null || name.isEmpty()) {
                holder.name.setText("Loading...");
                fetchName(doc, position);
            } else {
                holder.name.setText(name);
                nameCache.put(childId, name);
            }

            if (username != null && !username.isEmpty()) {
                holder.subtitle.setVisibility(View.VISIBLE);
                holder.subtitle.setText(username);
            } else {
                holder.subtitle.setVisibility(View.GONE);
            }
            holder.itemView.setOnClickListener(v -> callback.onClick(doc));
        }

        private void fetchName(DocumentSnapshot linkDoc, int position) {
            String childId = linkDoc.getId();
            String parentId = linkDoc.getString("parentId");

            // get data
            com.google.android.gms.tasks.Task<DocumentSnapshot> task;
            if (parentId != null && !parentId.isEmpty()) {
                task = db.collection("users").document(parentId)
                        .collection("children").document(childId).get();
            } else {
                task = db.collection("users").document(childId).get();
            }

            task.addOnSuccessListener(doc -> {
                String n = doc.getString("name");
                if (n != null && !n.isEmpty()) {
                    nameCache.put(childId, n);
                } else {
                    nameCache.put(childId, "Unknown");
                }
                notifyItemChanged(position);
            }).addOnFailureListener(e -> {
                nameCache.put(childId, "Unknown");
                notifyItemChanged(position);
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            TextView subtitle;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.child_name);
                subtitle = itemView.findViewById(R.id.child_subtitle);
            }
        }
    }
}
