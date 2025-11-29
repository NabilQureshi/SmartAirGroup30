package com.example.smartair.child_managent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.sharing.ManageSharingActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChooseChildForSharingActivity extends AppCompatActivity {

    private RecyclerView recyclerChildren;
    private ChildAdapter adapter;
    private List<Child> childList = new ArrayList<>();
    private ProgressBar loadingIndicator;

    private FirebaseFirestore db;
    private String parentId;
    private String mode = "sharing";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_child_for_sharing);

        db = FirebaseFirestore.getInstance();
        loadingIndicator = findViewById(R.id.loadingIndicator);
        recyclerChildren = findViewById(R.id.recyclerChildren);
        recyclerChildren.setLayoutManager(new LinearLayoutManager(this));

        resolveParentIdAndLoad();
    }

    private void resolveParentIdAndLoad() {
        String passedId = getIntent().getStringExtra("parentId");
        if (passedId != null) {
            parentId = passedId;
            setupAdapterAndLoad();
            return;
        }


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check child or parent
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "User profile not found!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String role = doc.getString("role");

                    if ("child".equals(role)) {
                        // child →  load parentId from child's doc
                        parentId = doc.getString("parentId");

                        if (parentId == null) {
                            Toast.makeText(this, "Child is not linked to any parent!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                    } else {
                        // parent  → use own UID
                        parentId = user.getUid();
                    }

                    setupAdapterAndLoad();
                });
    }

    /** Initializes adapter + click handling AFTER parentId is known */
    private void setupAdapterAndLoad() {

        String m = getIntent().getStringExtra("mode");
        if (m != null) mode = m;

        adapter = new ChildAdapter(childList, child -> {
            Intent intent;

            if ("manageChild".equals(mode)) {
                intent = new Intent(this, ManageChildActivity.class);
            } else {
                intent = new Intent(this, ManageSharingActivity.class);
            }

            intent.putExtra("childId", child.getUid());
            intent.putExtra("username", child.getUsername());
            intent.putExtra("password", child.getPassword());
            intent.putExtra("name", child.getName());
            intent.putExtra("dob", child.getDob());
            intent.putExtra("notes", child.getNotes());

            startActivity(intent);
        });

        recyclerChildren.setAdapter(adapter);

        listenChildrenRealtime();
    }

    /** ---------------------------------------------------------------
     * Real-time listener for children under the resolved parentId
     * --------------------------------------------------------------- */
    private void listenChildrenRealtime() {
        loadingIndicator.setVisibility(ProgressBar.VISIBLE);
        recyclerChildren.setVisibility(RecyclerView.GONE);

        db.collection("users")
                .document(parentId)
                .collection("children")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {

                        loadingIndicator.setVisibility(ProgressBar.GONE);
                        recyclerChildren.setVisibility(RecyclerView.VISIBLE);

                        if (e != null) {
                            Toast.makeText(ChooseChildForSharingActivity.this,
                                    "Error loading children: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        childList.clear();

                        if (snapshots == null || snapshots.isEmpty()) {
                            adapter.notifyDataSetChanged();
                            return;
                        }

                        for (QueryDocumentSnapshot doc : snapshots) {
                            Child child = doc.toObject(Child.class);
                            child.setUid(doc.getId());
                            childList.add(child);
                        }

                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
