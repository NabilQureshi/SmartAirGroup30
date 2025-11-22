package com.example.smartair.child_managent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;
import android.content.Intent;

import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewChildrenActivity extends AppCompatActivity {

    private RecyclerView recyclerChildren;
    private ChildAdapter adapter;
    private List<Child> childList = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_children_activity);

        recyclerChildren = findViewById(R.id.recyclerChildren);
        recyclerChildren.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ChildAdapter(childList, (child, childId) -> {
            Intent intent = new Intent(ViewChildrenActivity.this, ManageChildActivity.class);
            intent.putExtra("childId", childId);
            intent.putExtra("username", child.getUsername());
            intent.putExtra("password", child.getPassword());
            intent.putExtra("name", child.getName());
            intent.putExtra("dob", child.getDob());
            intent.putExtra("notes", child.getNotes());
            startActivity(intent);
        });




        recyclerChildren.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // IMPORTANT: check login
        if (user == null) {
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadChildren();
    }

    private void loadChildren() {
        String parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                .document(parentId)
                .collection("children")
                .addSnapshotListener((querySnapshot, error) -> {

                    if (error != null) {
                        Toast.makeText(this, "Error loading children.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    childList.clear();

                    if (querySnapshot == null || querySnapshot.isEmpty()) {
                        Toast.makeText(this, "No children found.", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Child child = doc.toObject(Child.class);
                        child.setId(doc.getId());
                        childList.add(child);
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
