package com.example.smartair;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChooseChildForSharingActivity extends AppCompatActivity {

    private RecyclerView recyclerChildren;
    private ChildAdapter adapter;
    private List<Child> childList = new ArrayList<>();
    private ProgressBar loadingIndicator;

    private FirebaseFirestore db;
    private String parentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_child_for_sharing);

        db = FirebaseFirestore.getInstance();
        parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadingIndicator = findViewById(R.id.loadingIndicator);
        recyclerChildren = findViewById(R.id.recyclerChildren);
        recyclerChildren.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChildAdapter(childList, (child, childId) -> {
            Intent intent = new Intent(this, ManageSharingActivity.class);
            intent.putExtra("childId", childId);
            startActivity(intent);
        });
        recyclerChildren.setAdapter(adapter);
        loadChildren();
    }

    private void loadChildren() {
        loadingIndicator.setVisibility(View.VISIBLE);
        recyclerChildren.setVisibility(View.GONE);

        db.collection("users")
                .document(parentId)
                .collection("children")
                .get()
                .addOnSuccessListener(snapshot -> {
                    loadingIndicator.setVisibility(View.GONE);
                    recyclerChildren.setVisibility(View.VISIBLE);
                    if (snapshot.isEmpty()) {
                        Toast.makeText(this, "No children added yet!", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    childList.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Child child = doc.toObject(Child.class);
                        child.setId(doc.getId());
                        childList.add(child);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    loadingIndicator.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading children: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
